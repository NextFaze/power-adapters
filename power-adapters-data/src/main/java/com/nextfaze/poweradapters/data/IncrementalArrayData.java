package com.nextfaze.poweradapters.data;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import lombok.NonNull;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.max;
import static java.lang.Thread.currentThread;

/**
 * Mutable {@link Data} implementation backed by an {@link ArrayList}, which is loaded incrementally until the source
 * has no more data. Cannot contain {@code null} elements. Not thread-safe.
 * @param <T> The type of element this data contains.
 */
public abstract class IncrementalArrayData<T> extends AbstractData<T> implements List<T> {

    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new NamedThreadFactory("Incremental Array Data Thread %d");

    @NonNull
    private final NotifyingArrayList<T> mData = new NotifyingArrayList<>(this);

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

    private boolean mLoading;
    private int mAvailable = Integer.MAX_VALUE;
    private boolean mDirty = true;
    private boolean mClear;

    protected IncrementalArrayData() {
        this(DEFAULT_THREAD_FACTORY);
    }

    protected IncrementalArrayData(@NonNull ThreadFactory threadFactory) {
        mThreadFactory = threadFactory;
    }

    @CallSuper
    public void close() {
        stopThread();
        mData.clear();
        mData.trimToSize();
    }

    @Override
    public final int size() {
        return mData.size();
    }

    @Override
    public final boolean isEmpty() {
        return mData.isEmpty();
    }

    @Override
    public final boolean contains(@Nullable Object object) {
        return mData.contains(object);
    }

    @Override
    public final int indexOf(@Nullable Object object) {
        return mData.indexOf(object);
    }

    @Override
    public final int lastIndexOf(@Nullable Object object) {
        return mData.lastIndexOf(object);
    }

    @Override
    public final T remove(int index) {
        return mData.remove(index);
    }

    @Override
    public final boolean add(@NonNull T t) {
        return mData.add(t);
    }

    @Override
    public final void add(int index, T object) {
        mData.add(index, object);
    }

    @Override
    public final boolean addAll(@NonNull Collection<? extends T> collection) {
        return mData.addAll(collection);
    }

    @Override
    public final boolean addAll(int index, @NonNull Collection<? extends T> collection) {
        return mData.addAll(index, collection);
    }

    @Override
    public final boolean remove(@NonNull Object obj) {
        return mData.remove(obj);
    }

    @UiThread
    @NonNull
    @Override
    public final ListIterator<T> listIterator() {
        return mData.listIterator();
    }

    @UiThread
    @NonNull
    @Override
    public final ListIterator<T> listIterator(int location) {
        return mData.listIterator(location);
    }

    @UiThread
    @NonNull
    @Override
    public final List<T> subList(int start, int end) {
        return mData.subList(start, end);
    }

    @UiThread
    @Override
    public final boolean containsAll(@NonNull Collection<?> collection) {
        return mData.containsAll(collection);
    }

    @Override
    public final boolean removeAll(@NonNull Collection<?> collection) {
        return mData.removeAll(collection);
    }

    @Override
    public final boolean retainAll(@NonNull Collection<?> collection) {
        return mData.retainAll(collection);
    }

    @Override
    public final T set(int index, T object) {
        return mData.set(index, object);
    }

    @UiThread
    @NonNull
    @Override
    public final Object[] toArray() {
        return mData.toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @UiThread
    @NonNull
    @Override
    public final <E> E[] toArray(@NonNull E[] contents) {
        return mData.toArray(contents);
    }

    @UiThread
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

    @UiThread
    @Override
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

    /** Set the number of rows to "look ahead" before loading automatically. */
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

    private void runLoadLoop() {
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
            } catch (Throwable e) {
                notifyError(e);
                mError = true;
                setLoading(false);
            }

            // Block until instructed to continue, even if an error occurred.
            // In this case, loading must be explicitly resumed.
            block();
        }
    }

    private void overwriteResult(@NonNull List<? extends T> result) {
        mClear = false;
        onClear();
        mData.clear();
        mData.addAll(result);
    }

    private void appendResult(@NonNull List<? extends T> result) {
        mData.addAll(result);
    }

    private void setLoading(final boolean loading) {
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
