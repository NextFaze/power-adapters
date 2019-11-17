package com.nextfaze.poweradapters.data;

import com.nextfaze.poweradapters.internal.NotifyingArrayList;

import java.io.Closeable;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;
import static java.lang.Math.max;
import static java.lang.Thread.currentThread;

/**
 * Mutable {@link Data} implementation backed by an {@link ArrayList}, which is loaded incrementally until the source
 * has no more data. Cannot contain {@code null} elements. Not thread-safe.
 * @param <T> The type of element this data contains.
 */
public abstract class IncrementalArrayData<T> extends Data<T> implements Closeable {

    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new NamedThreadFactory("Incremental Array Data Thread %d");

    @NonNull
    private final NotifyingArrayList<T> mData = new NotifyingArrayList<>(mDataObservable);

    @NonNull
    private final ThreadFactory mThreadFactory;

    @NonNull
    private final Lock mLock = new ReentrantLock();

    @NonNull
    private final Condition mLoad = mLock.newCondition();

    /** The number of rows to look ahead before loading. */
    private volatile int mLookAheadRowCount = 5;

    @Nullable
    private Thread mThread;

    /** Indicates the last attempt to load a page failed. */
    private volatile boolean mError;

    boolean mLoading;
    int mAvailable = Integer.MAX_VALUE;
    private boolean mDirty = true;
    private boolean mClear;

    protected IncrementalArrayData() {
        this(DEFAULT_THREAD_FACTORY);
    }

    @SuppressWarnings("WeakerAccess")
    protected IncrementalArrayData(@NonNull ThreadFactory threadFactory) {
        mThreadFactory = checkNotNull(threadFactory, "threadFactory");
    }

    @CallSuper
    @Override
    public void close() {
        stopThread();
        mData.clear();
        mData.trimToSize();
    }

    @Override
    public final int size() {
        return mData.size();
    }

    @NonNull
    @Override
    public final T get(int position, int flags) {
        // Requested end of data? Time to load more.
        // The presence of the presentation flag indicates this is a good time to continue loading elements.
        if ((flags & FLAG_PRESENTATION) != 0 && position >= size() - 1 - mLookAheadRowCount) {
            proceed();
        }
        return mData.get(position);
    }

    public final void clear() {
        mClear = false;
        onClear();
        mData.clear();
    }

    @Override
    public final void invalidate() {
        mDirty = true;
        mClear = true;
    }

    @Override
    public final void refresh() {
        stopThread();
        mDirty = true;
        if (!startThreadIfNeeded()) {
            setLoading(false);
        }
    }

    @Override
    public final void reload() {
        clear();
        refresh();
    }

    /** Load the next increment of elements. */
    @UiThread
    public final void loadNext() {
        proceed();
    }

    @UiThread
    public final int getLookAheadRowCount() {
        return mLookAheadRowCount;
    }

    /** Set the number of rows to "look ahead" before loading automatically. A negative value disables look ahead. */
    @UiThread
    public final void setLookAheadRowCount(int lookAheadRowCount) {
        mLookAheadRowCount = lookAheadRowCount;
    }

    @Override
    public final boolean isLoading() {
        return mLoading;
    }

    @Override
    public final int available() {
        return mAvailable;
    }

    /**
     * Returns this {@link IncrementalArrayData} as a mutable list. Operations performed on the returned {@link List}
     * are reflected in this {@link IncrementalArrayData}, and the correct notifications will be issued.
     */
    @NonNull
    @Override
    public List<T> asList() {
        return mData;
    }

    @CallSuper
    @Override
    protected void onFirstDataObserverRegistered() {
        super.onFirstDataObserverRegistered();
        if (mError) {
            // Last attempt to load an increment failed, so try again now we've become visible again.
            proceed();
        }
        if (mClear) {
            mClear = false;
            onClear();
            mData.clear();
        }
        startThreadIfNeeded();
    }

    /**
     * Called from a worker thread to load the next increment of items.
     * @return A result containing the next set of elements to be appended, or {@code null} if there are no more items.
     * The result also indicates if these are the final elements of the data set.
     * @throws Throwable If any error occurs while trying to load.
     */
    @WorkerThread
    @Nullable
    protected abstract Result<? extends T> load() throws Throwable;

    /** Called prior to elements being cleared. Always called from the UI thread. */
    @UiThread
    protected void onClear() {
    }

    /** Called when loading is about to begin from the start. Always called from the UI thread. */
    @UiThread
    protected void onLoadBegin() {
    }

    private boolean startThreadIfNeeded() {
        if (mDirty && mThread == null && getDataObserverCount() > 0) {
            mDirty = false;
            onLoadBegin();
            setLoading(true);
            mThread = mThreadFactory.newThread(new Runnable() {
                @Override
                public void run() {
                    runLoadLoop();
                }
            });
            mThread.start();
            return true;
        }
        return false;
    }

    private void stopThread() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

    void runLoadLoop() {
        try {
            loadLoop();
        } catch (InterruptedException e) {
            // Normal thread termination.
        }
    }

    /**
     * Loads each increment until full range has been loading, halting in between increment until instructed to
     * proceed.
     */
    private void loadLoop() throws InterruptedException {
        boolean firstItem = true;
        boolean moreAvailable = true;

        // Loop until all loaded.
        while (moreAvailable) {
            // Thread interruptions terminate the loop.
            if (currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            try {
                setLoading(true);

                // Load next increment of items.
                final Result<? extends T> result = load();
                moreAvailable = result != null && result.getRemaining() > 0;
                setAvailable(result != null ? result.getRemaining() : 0);

                // If invalidated while shown, we lazily clear the data so the user doesn't see blank data while loading.
                final boolean needToClear = firstItem;
                firstItem = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<? extends T> elements =
                                result != null ? result.getElements() : Collections.<T>emptyList();
                        if (needToClear) {
                            overwriteResult(elements);
                        } else {
                            appendResult(elements);
                        }
                        setLoading(false);
                    }
                });
            } catch (InterruptedException e) {
                throw e;
            } catch (InterruptedIOException e) {
                InterruptedException interruptedException = new InterruptedException();
                interruptedException.initCause(e);
                throw interruptedException;
            } catch (final Throwable e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyError(e);
                    }
                });
                mError = true;
                setLoading(false);
            }

            // Block until instructed to continue, even if an error occurred.
            // In this case, loading must be explicitly resumed.
            block();
        }
    }

    void overwriteResult(@NonNull List<? extends T> result) {
        mClear = false;
        onClear();
        mData.replaceAll(result);
    }

    void appendResult(@NonNull List<? extends T> result) {
        mData.addAll(result);
    }

    void setLoading(final boolean loading) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLoading != loading) {
                    mLoading = loading;
                    notifyLoadingChanged();
                }
            }
        });
    }

    private void setAvailable(final int available) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAvailable != available) {
                    mAvailable = available;
                    notifyAvailableChanged();
                }
            }
        });
    }

    private void block() throws InterruptedException {
        mLock.lock();
        try {
            mLoad.await();
        } finally {
            mLock.unlock();
        }
    }

    private void proceed() {
        mError = false;
        mLock.lock();
        try {
            mLoad.signal();
        } finally {
            mLock.unlock();
        }
    }

    public static final class Result<T> {

        @SuppressWarnings("unchecked")
        private static final Result NONE_REMAINING = new Result(Collections.emptyList(), 0);

        @NonNull
        private final List<? extends T> mElements;

        /** Indicates how many more elements available to be loaded after this. */
        private final int mRemaining;

        public Result(@NonNull List<? extends T> elements, int remaining) {
            mElements = elements;
            mRemaining = max(0, remaining);
        }

        @NonNull
        public static <T> Result<T> moreRemaining(@NonNull List<? extends T> list) {
            return new Result<>(list, Integer.MAX_VALUE);
        }

        @SuppressWarnings("unchecked")
        @NonNull
        public static <T> Result<T> noneRemaining() {
            return NONE_REMAINING;
        }

        @NonNull
        public List<? extends T> getElements() {
            return mElements;
        }

        public int getRemaining() {
            return mRemaining;
        }
    }
}
