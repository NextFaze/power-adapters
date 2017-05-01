package com.nextfaze.poweradapters.data;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;

import static android.os.Looper.getMainLooper;
import static java.lang.Math.abs;
import static java.lang.Math.min;

/**
 * Presents the contents of a {@link Cursor}
 * <p>
 * {@linkplain Cursor} instances are loaded asynchronously in a worker thread using an {@link ExecutorService}. Callers
 * should not close the cursors themselves, as this class will manage closing them.
 * @param <T> The type of object presented by this data.
 */
abstract class CursorData<T> extends SimpleData<T, Cursor> implements Closeable {

    /** The delay in milliseconds after which a release will be performed, while we're not observed. */
    private static final long RELEASE_DELAY = 3000;

    /** Handler used to perform delayed tasks. */
    @NonNull
    private final Handler mHandler = new Handler(getMainLooper());

    /** Observes the data of the current cursor. */
    @NonNull
    private final ContentObserver mCursorContentObserver = new ContentObserver(null) {
        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange) {
            // Postpone refresh until next event loop to avoid ConcurrentModificationException
            // thrown by shoddy ContentObservable observer iteration.
            postRefresh();
        }
    };

    /** Runnable that performs a {@link #release()}. */
    @NonNull
    private final Runnable mReleaseRunnable = new Runnable() {
        @Override
        public void run() {
            release();
        }
    };

    /** Runnable that performs a {@link #refresh()}. */
    @NonNull
    private final Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            refresh();
        }
    };

    /** The current cursor providing the data set. */
    @Nullable
    private Cursor mCursor;

    /** The cursor to which the observer is registered. */
    @Nullable
    private Cursor mObservedCursor;

    protected CursorData() {
        this(DataExecutors.defaultExecutor());
    }

    protected CursorData(@NonNull ExecutorService executor) {
        super(executor);
    }

    /** Invoked to map the {@link Cursor} at its current position to an instance of {@link T}. */
    @NonNull
    protected abstract T map(@NonNull Cursor cursor);

    /**
     * Called by this {@linkplain CursorData} in order to load the next data set.
     * The returned {@linkplain Cursor} is managed by this {@linkplain CursorData} and does not need to be closed.
     * @return A cursor with an updated data set.
     * @throws Throwable If the cursor fails to load.
     */
    @NonNull
    protected abstract Cursor load() throws Throwable;

    @NonNull
    @Override
    public final T get(int position, int flags) {
        if (mCursor == null) {
            throw new NullPointerException("cursor");
        }
        mCursor.moveToPosition(position);
        return map(mCursor);
    }

    @Override
    public final int size() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    /** Closes this {@linkplain CursorData}, which closes the {@link Cursor} managed by it. */
    @CallSuper
    @Override
    public void close() {
        cancelTask();
        closeCursor();
    }

    @CallSuper
    @Override
    protected void onFirstDataObserverRegistered() {
        super.onFirstDataObserverRegistered();
        // Cancel any pending release
        cancelRelease();
        if (mCursor == null) {
            // We're being observed now, so refresh the cursor, as the data set may have changed
            refresh();
        }
    }

    @CallSuper
    @Override
    protected void onLastDataObserverUnregistered() {
        super.onLastDataObserverUnregistered();
        // We're no longer being observed, so cease loading/observing the cursor, and release it.
        // A delay is imposed to prevent releasing the cursor for config changes.
        releaseDelayed();
    }

    /** Performs a {@link #release()} after a delay. */
    private void releaseDelayed() {
        mHandler.removeCallbacks(mReleaseRunnable);
        mHandler.postDelayed(mReleaseRunnable, RELEASE_DELAY);
    }

    private void cancelRelease() {
        mHandler.removeCallbacks(mReleaseRunnable);
    }

    /** Releases the cursor and cancels any in-flight load tasks. */
    void release() {
        cancelTask();
        closeCursor();
    }

    void postRefresh() {
        mHandler.post(mRefreshRunnable);
    }

    /** Switches to the specified cursor, notifying observers of changes as necessary. */
    private void changeCursor(@Nullable Cursor newCursor) {
        Cursor oldCursor = mCursor;
        if (mCursor != newCursor) {
            int oldSize = 0;
            int deltaSize = 0;
            if (oldCursor != null) {
                int oldCount = oldCursor.getCount();
                oldSize = oldCount;
                deltaSize -= oldCount;
                oldCursor.close();
            }
            mCursor = newCursor;
            int newSize = 0;
            if (newCursor != null) {
                int newCount = newCursor.getCount();
                deltaSize += newCount;
                newSize = newCount;
            }
            updateCursorObserver();
            int changed = min(oldSize, newSize);
            if (changed > 0) {
                notifyItemRangeChanged(0, changed);
            }
            if (deltaSize < 0) {
                notifyItemRangeRemoved(oldSize + deltaSize, abs(deltaSize));
            } else if (deltaSize > 0) {
                notifyItemRangeInserted(oldSize, abs(deltaSize));
            }
        }
    }

    @NonNull
    @Override
    final Cursor loadDataSet() throws Throwable {
        Cursor cursor = load();
        // Invoke getCount() to ensure the Cursor implementation fill its window, which can be time consuming.
        // If we didn't do this, the fill might take place later on the main thread, during a call to size() or get().
        cursor.getCount();
        return cursor;
    }

    @Override
    final void onNewDataSet(@Nullable Cursor newCursor) {
        changeCursor(newCursor);
    }

    /** Registers or unregisters with the cursor. Idempotent. */
    private void updateCursorObserver() {
        Cursor newObservedCursor = getDataObserverCount() > 0 ? mCursor : null;
        if (newObservedCursor != mObservedCursor) {
            if (mObservedCursor != null) {
                mObservedCursor.unregisterContentObserver(mCursorContentObserver);
            }
            mObservedCursor = newObservedCursor;
            if (mObservedCursor != null) {
                mObservedCursor.registerContentObserver(mCursorContentObserver);
            }
        }
    }

    /** Closes the cursor, notifying observers of data changes as necessary. */
    private void closeCursor() {
        if (mCursor != null) {
            int count = mCursor.getCount();
            mCursor.close();
            mCursor = null;
            notifyItemRangeRemoved(0, count);
        }
        updateCursorObserver();
    }
}
