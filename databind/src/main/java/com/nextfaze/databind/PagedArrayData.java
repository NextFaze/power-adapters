package com.nextfaze.databind;

import android.util.SparseArray;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.InterruptedIOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Thread.currentThread;

@Slf4j
@Accessors(prefix = "m")
public abstract class PagedArrayData<T> extends AbstractData<T> {

    public static final int ROWS_PER_REQUEST_DEFAULT = 20;

    private static final int UNKNOWN = -1;
    private static final int ROWS_PER_REQUEST_MIN = 1;
    private static final int ROWS_PER_REQUEST_MAX = MAX_VALUE;
    private static final long HIDDEN_DURATION_INVALIDATE_THRESHOLD = 10 * 1000;
    private static final String THREAD_NAME = PagedArrayData.class.getSimpleName() + " Thread";

    /** The backing storage. May contain safely contain holes. */
    @NonNull
    private final SparseArray<T> mData = new SparseArray<T>();

    @NonNull
    private final Lock mLock = new ReentrantLock();

    @NonNull
    private final Condition mLoad = mLock.newCondition();

    /** The number of rows to be requested. */
    @Getter
    private final int mRowsPerRequest;

    @Nullable
    private Thread mThread;

    private volatile boolean mLoading;

    protected PagedArrayData() {
        this(ROWS_PER_REQUEST_DEFAULT);
    }

    protected PagedArrayData(int rowsPerRequest) {
        mRowsPerRequest = clamp(rowsPerRequest, ROWS_PER_REQUEST_MIN, ROWS_PER_REQUEST_MAX);
    }

    @NonNull
    @Override
    public final T get(int position) {
        // Requested end of data? Time to load more.
        if (position >= mData.size() - 1) {
            proceed();
        }
        return mData.get(position);
    }

    @Override
    public final int size() {
        return mData.size();
    }

    @Override
    public final boolean isLoading() {
        return mLoading;
    }

    public final void clear() {
        stopLoadThread();
        startLoadThreadIfNecessary();
        mData.clear();
        notifyChanged();
    }

    public final void loadNext() {
        proceed();
    }

    @NonNull
    protected abstract Page<T> load(int offset, int count) throws Exception;

    @Override
    protected void onShown(long millisHidden) {
        if (millisHidden >= HIDDEN_DURATION_INVALIDATE_THRESHOLD) {
            mData.clear();
            stopLoadThread();
        }
        startLoadThreadIfNecessary();
    }

    @Override
    protected void onHidden(long millisShown) {
        // TODO: Cancel after a delay.
        stopLoadThread();
    }

    private void startLoadThreadIfNecessary() {
        if (mThread == null) {
            mThread = new Thread(THREAD_NAME) {
                @Override
                public void run() {
                    try {
                        loadLoop();
                    } catch (InterruptedException e) {
                        // Normal thread termination.
                    }
                }
            };
            mThread.start();
        }
    }

    private void stopLoadThread() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

    /** Loads each page until full range has been loading, halting in between pages until instructed to proceed. */
    private void loadLoop() throws InterruptedException {
        int lastLoadedIndex = 0;
        int total = UNKNOWN;

        // Loop until full range loaded.
        while (total == UNKNOWN || lastLoadedIndex < total) {
            // Thread interruptions terminate the loop.
            if (currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            try {
                // Calculate page to be loaded next.
                int remaining = total != UNKNOWN ? total - lastLoadedIndex : MAX_VALUE;
                int offset = lastLoadedIndex;
                int count = min(mRowsPerRequest, remaining);

                mLoading = true;
                notifyChanged();

                final Page<T> page;
                try {
                    // Load next page.
                    page = load(offset, count);
                } finally {
                    mLoading = false;
                    notifyChanged();
                }

                total = page.getTotal();
                lastLoadedIndex += page.getCount();

                // Store page and notify users of change.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        store(page);
                        notifyChanged();
                    }
                });
            } catch (InterruptedException e) {
                // Normal thread interruption.
                throw new InterruptedException();
            } catch (InterruptedIOException e) {
                // Normal thread interruption from blocking I/O.
                throw new InterruptedException();
            } catch (Throwable e) {
                log.error("Error loading page", e);
                notifyError(e);
            }

            // Block until instructed to continue, even if an error occurred.
            // In this case, loading must be explicitly resumed.
            block();
        }
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
        mLock.lock();
        try {
            mLoad.signal();
        } finally {
            mLock.unlock();
        }
    }

    private void store(@NonNull Page<T> page) {
        List<T> list = page.getContents();
        int size = list.size();
        for (int i = 0; i < size && i < page.getCount(); ++i) {
            T item = list.get(i);
            int position = page.getOffset() + i;
            mData.put(position, item);
        }
    }

    private static int clamp(int v, int min, int max) {
        return max(min(max, v), min);
    }

    @Getter
    @Accessors(prefix = "m")
    public static final class Page<T> {

        @NonNull
        private final List<T> mContents;

        private final int mOffset;
        private final int mCount;
        private final int mTotal;

        public Page(@NonNull List<T> contents, int offset, int count, int total) {
            mContents = contents;
            mOffset = offset;
            mCount = count;
            mTotal = total;
        }

        @NonNull
        public static <T> Page<T> emptyResult() {
            return new Page<T>(Collections.<T>emptyList(), 0, 0, 0);
        }
    }
}
