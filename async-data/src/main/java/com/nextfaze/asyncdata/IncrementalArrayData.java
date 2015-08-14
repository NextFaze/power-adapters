package com.nextfaze.asyncdata;

import android.support.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@Accessors(prefix = "m")
public abstract class IncrementalArrayData<T> extends AbstractData<T> implements List<T> {

    private static final Logger log = LoggerFactory.getLogger(IncrementalArrayData.class);

    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new NamedThreadFactory("Incremental Array Data Thread %d");

    @NonNull
    private final ArrayList<T> mData = new ArrayList<>();

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

    // TODO: It makes more sense to auto invalidate after X millis since last load, rather than duration hidden.

    /** Automatically invalidate contents if data is hidden for the specified duration. */
    private long mAutoInvalidateDelay = Long.MAX_VALUE;

    /** Indicates the last attempt to load a page failed. */
    private volatile boolean mError;

    private boolean mLoading;
    private int mAvailable = Integer.MAX_VALUE;
    private boolean mDirty = true;

    protected IncrementalArrayData() {
        this(DEFAULT_THREAD_FACTORY);
    }

    protected IncrementalArrayData(@NonNull ThreadFactory threadFactory) {
        mThreadFactory = threadFactory;
    }

    /** Subclasses must call through to super. */
    @Override
    protected void onClose() throws Throwable {
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
    public final boolean contains(Object object) {
        return mData.contains(object);
    }

    @Override
    public final int indexOf(Object object) {
        return mData.indexOf(object);
    }

    @Override
    public final int lastIndexOf(Object object) {
        return mData.lastIndexOf(object);
    }

    @Override
    public final T remove(int index) {
        T removed = mData.remove(index);
        notifyItemRemoved(index);
        return removed;
    }

    @Override
    public final boolean add(@NonNull T t) {
        if (mData.add(t)) {
            notifyItemInserted(mData.size() - 1);
            return true;
        }
        return false;
    }

    @Override
    public final void add(int index, T object) {
        mData.add(index, object);
        notifyItemInserted(index);
    }

    @Override
    public final boolean addAll(@NonNull Collection<? extends T> collection) {
        int oldSize = mData.size();
        mData.addAll(collection);
        int newSize = mData.size();
        if (newSize != oldSize) {
            int count = mData.size() - oldSize;
            notifyItemRangeInserted(oldSize, count);
            return true;
        }
        return false;
    }

    @Override
    public final boolean addAll(int index, @NonNull Collection<? extends T> collection) {
        int oldSize = mData.size();
        mData.addAll(index, collection);
        int newSize = mData.size();
        if (newSize != oldSize) {
            int count = mData.size() - oldSize;
            notifyItemRangeInserted(index, count);
            return true;
        }
        return false;
    }

    @Override
    public final boolean remove(@NonNull Object obj) {
        //noinspection SuspiciousMethodCalls
        int index = mData.indexOf(obj);
        if (index != -1) {
            mData.remove(index);
            notifyItemRemoved(index);
            return true;
        }
        return false;
    }

    // TODO: Notify of change if modified from iterator.

    @NonNull
    @Override
    public final ListIterator<T> listIterator() {
        return mData.listIterator();
    }

    @NonNull
    @Override
    public final ListIterator<T> listIterator(int location) {
        return mData.listIterator(location);
    }

    @NonNull
    @Override
    public final List<T> subList(int start, int end) {
        return mData.subList(start, end);
    }

    @Override
    public final boolean containsAll(@NonNull Collection<?> collection) {
        return mData.containsAll(collection);
    }

    @Override
    public final boolean removeAll(@NonNull Collection<?> collection) {
        boolean removed = mData.removeAll(collection);
        if (removed) {
            // TODO: Fine-grained change notification.
            notifyDataChanged();
        }
        return removed;
    }

    @Override
    public final boolean retainAll(@NonNull Collection<?> collection) {
        boolean changed = mData.retainAll(collection);
        if (changed) {
            // TODO: Fine-grained change notification.
            notifyDataChanged();
        }
        return changed;
    }

    @Override
    public final T set(int index, T object) {
        T t = mData.set(index, object);
        notifyItemChanged(index);
        return t;
    }

    @NonNull
    @Override
    public final Object[] toArray() {
        return mData.toArray();
    }

    @NonNull
    @Override
    public final <T> T[] toArray(@NonNull T[] contents) {
        return mData.toArray(contents);
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

    @Override
    public final void clear() {
        clearElementsAndNotify();
    }

    @Override
    public final void invalidate() {
        mDirty = true;
    }

    @Override
    public final void refresh() {
        mDirty = true;
        setAvailable(Integer.MAX_VALUE);
        stopThread();
        startThreadIfNeeded();
    }

    @Override
    public final void reload() {
        clear();
        refresh();
    }

    /** Load the next increment of elements. */
    public final void loadNext() {
        proceed();
    }

    public final int getLookAheadRowCount() {
        return mLookAheadRowCount;
    }

    /** Set the number of rows to "look ahead" before loading automatically. */
    public final void setLookAheadRowCount(int lookAheadRowCount) {
        mLookAheadRowCount = lookAheadRowCount;
    }

    public final long getAutoInvalidateDelay() {
        return mAutoInvalidateDelay;
    }

    /** Automatically invalidate contents if data is hidden for the specified duration. */
    public final void setAutoInvalidateDelay(long autoInvalidateDelay) {
        mAutoInvalidateDelay = autoInvalidateDelay;
    }

    @Override
    public final boolean isLoading() {
        return mLoading;
    }

    @Override
    public final int available() {
        return mAvailable;
    }

    @Override
    protected final void onShown(long millisHidden) {
        log.trace("Shown after being hidden for {} ms", millisHidden);
        if (mError) {
            // Last attempt to load an increment failed, so try again now we've become visible again.
            proceed();
        }
        if (millisHidden >= mAutoInvalidateDelay) {
            log.trace("Automatically invalidating due to auto-invalidate delay being reached or exceeded");
            mDirty = true;
        }
        if (mDirty) {
            stopThread();
            clearElementsAndNotify();
        }
        startThreadIfNeeded();
    }

    @Override
    protected final void onHidden(long millisShown) {
        log.trace("Hidden after being shown for {} ms", millisShown);
    }

    @Override
    protected void onHideTimeout() {
        log.trace("Hide timeout elapsed ({} ms); clearing and stopping thread", getHideTimeout());
        clearElementsAndNotify();
        stopThread();
    }

    /**
     * Load the next increment of items.
     * @return A result containing the next set of elements to be appended, or {@code null} if there are no more items.
     * The result also indicates if these are the final elements of the data set.
     * @throws Throwable If any error occurs while trying to load.
     */
    @Nullable
    protected abstract Result<? extends T> load() throws Throwable;

    /** Called prior to elements being cleared. Always called from the UI thread. */
    protected void onClear() {
    }

    /** Called when loading is about to begin from the start. Always called from the UI thread. */
    protected void onLoadBegin() {
    }

    private void clearElementsAndNotify() {
        int size = mData.size();
        if (size > 0) {
            onClear();
            mData.clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    private void startThreadIfNeeded() {
        if (mDirty && mThread == null && isShown()) {
            mDirty = false;
            log.trace("Starting thread");
            onLoadBegin();
            setLoading(true);
            mThread = mThreadFactory.newThread(new Runnable() {
                @Override
                public void run() {
                    runLoadLoop();
                }
            });
            mThread.start();
        }
    }

    private void stopThread() {
        if (mThread != null) {
            log.trace("Stopping thread");
            mThread.interrupt();
            mThread = null;
        }
    }

    private void runLoadLoop() {
        try {
            loadLoop();
        } catch (InterruptedException e) {
            // Normal thread termination.
            log.trace("Thread terminated normally with an InterruptedException");
        }
    }

    /**
     * Loads each increment until full range has been loading, halting in between increment until instructed to
     * proceed.
     */
    private void loadLoop() throws InterruptedException {
        log.trace("Start load loop");
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

                log.trace("Loading next increment");

                // Load next increment of items.
                final Result<? extends T> result = load();
                moreAvailable = result != null && result.getRemaining() > 0;
                setAvailable(result != null ? result.getRemaining() : 0);

                if (result != null && !result.getElements().isEmpty()) {
                    // If invalidated while shown, we lazily clear the data so the user doesn't see blank data while loading.
                    final boolean needToClear = firstItem;
                    firstItem = false;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (needToClear) {
                                clearElementsAndNotify();
                            }
                            int positionStart = mData.size();
                            int insertCount = 0;
                            for (T t : result.getElements()) {
                                if (t != null) {
                                    mData.add(t);
                                    insertCount++;
                                }
                            }
                            if (insertCount > 0) {
                                notifyItemRangeInserted(positionStart, insertCount);
                            }
                        }
                    });
                }
            } catch (InterruptedException | InterruptedIOException e) {
                throw new InterruptedException();
            } catch (Throwable e) {
                log.error("Error loading", e);
                notifyError(e);
                mError = true;
            } finally {
                setLoading(false);
            }

            // Block until instructed to continue, even if an error occurred.
            // In this case, loading must be explicitly resumed.
            block();
        }
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

    @Getter
    @Accessors(prefix = "m")
    public static final class Result<T> {

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

        @NonNull
        public static <T> Result<T> noneRemaining() {
            return new Result<>(Collections.<T>emptyList(), 0);
        }
    }
}
