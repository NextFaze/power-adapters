package com.nextfaze.databind;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.max;
import static java.lang.Thread.currentThread;

@Accessors(prefix = "m")
public abstract class IncrementalArrayData<T> extends AbstractData<T> {

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
    @Getter
    private volatile int mLookAheadRowCount = 5;

    @Nullable
    private Thread mThread;

    /** Automatically invalidate contents if data is hidden for the specified duration. */
    @Getter
    @Setter
    private long mAutoInvalidateDelay = Long.MAX_VALUE;

    /** Indicates the last attempt to load a page failed. */
    private volatile boolean mError;

    private boolean mLoading;
    private boolean mDirty = true;

    protected IncrementalArrayData() {
        this(DEFAULT_THREAD_FACTORY);
    }

    protected IncrementalArrayData(@NonNull ThreadFactory threadFactory) {
        mThreadFactory = threadFactory;
    }

    @NonNull
    @Override
    public final T get(int position) {
        // Requested end of data? Time to load more.
        if (position >= size() - mLookAheadRowCount) {
            proceed();
        }
        return getItem(position);
    }

    @Override
    public final boolean isLoading() {
        return mLoading;
    }

    /** Flags the data to be cleared and reloaded next time it is "shown". */
    public final void invalidateDeferred() {
        mDirty = true;
    }

    /** Clears the contents, and starts loading again if data is currently shown. */
    public final void clear() {
        clearDataAndNotify();
        stopThread();
        startThreadIfNeeded();
    }

    public final void loadNext() {
        proceed();
    }

    public final void setLookAheadRowCount(int lookAheadRowCount) {
        mLookAheadRowCount = max(0, lookAheadRowCount);
    }

    @Override
    protected void onShown(long millisHidden) {
        log.trace("Shown after being hidden for {} ms", millisHidden);
        if (mError) {
            // Last attempt to load a page failed, so try again now we've become visible again.
            proceed();
        }
        if (millisHidden >= mAutoInvalidateDelay) {
            log.trace("Automatically invalidating due to auto-invalidate delay being reached or exceeded");
            mDirty = true;
        }
        if (mDirty) {
            // Data is dirty, so reload everything.
            clearDataAndNotify();
            stopThread();
        }
        startThreadIfNeeded();
    }

    @Override
    protected void onClose() throws Exception {
        stopThread();
    }

    /**
     * Load the next set of items.
     * @return A list containing the next set of items to be appended, or {@code null} if there are no more items.
     * @throws Throwable If any error occurs while trying to load.
     */
    @Nullable
    protected abstract List<? extends T> load() throws Throwable;

    /** Called when data is invalidated or cleared. May happen on any thread. */
    protected void onInvalidate() {
    }

    private void append(@NonNull List<? extends T> list) {
        for (T t : list) {
            if (t != null) {
                appendItem(t);
            }
        }
    }

    private void appendItem(@NonNull T t) {
        mData.add(t);
    }

    @NonNull
    private T getItem(int position) {
        return mData.get(position);
    }

    private void clearItems() {
        mData.clear();
    }

    @Override
    public final int size() {
        return mData.size();
    }

    private void clearDataAndNotify() {
        if (size() > 0) {
            clearItems();
            onInvalidate();
            notifyChanged();
        }
    }

    private void startThreadIfNeeded() {
        if (mThread == null && isShown()) {
            log.trace("Starting thread");
            mDirty = false;
            setLoading(true);
            mThread = mThreadFactory.newThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        loadLoop();
                    } catch (InterruptedException e) {
                        // Normal thread termination.
                        log.trace("Thread terminated normally with an InterruptedException");
                    }
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

    /** Loads each page until full range has been loading, halting in between pages until instructed to proceed. */
    private void loadLoop() throws InterruptedException {
        log.trace("Start load loop");
        boolean hasMore = true;

        // Loop until all loaded.
        while (hasMore) {
            // Thread interruptions terminate the loop.
            if (currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            try {
                setLoading(true);

                log.trace("Loading next chunk of items");

                // Load next items.
                final List<? extends T> items = load();
                hasMore = items != null;

                // Store items and notify of change.
                if (items != null && !items.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            append(items);
                            notifyChanged();
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
}
