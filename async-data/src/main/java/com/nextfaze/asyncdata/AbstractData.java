package com.nextfaze.asyncdata;

import android.os.Handler;
import android.os.Looper;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static android.os.Looper.getMainLooper;
import static android.os.SystemClock.elapsedRealtime;

/**
 * Skeleton {@link Data} implementation that provides observer management, hide timeout functionality, shown/hidden
 * state tracking, and other sensible default method implementations.
 */
@Accessors(prefix = "m")
public abstract class AbstractData<T> implements Data<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractData.class);

    public static final long NEVER = -1;

    private static final long HIDE_TIMEOUT_DEFAULT = NEVER;

    @NonNull
    private final DataObservers mDataObservers = new DataObservers();

    @NonNull
    private final AvailableObservers mAvailableObservers = new AvailableObservers();

    @NonNull
    private final LoadingObservers mLoadingObservers = new LoadingObservers();

    @NonNull
    private final ErrorObservers mErrorObservers = new ErrorObservers();

    @NonNull
    private final Handler mHandler = new Handler(getMainLooper());

    @NonNull
    private final CoalescingPoster mPoster = new CoalescingPoster(mHandler);

    @NonNull
    private final Runnable mHideTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mClosed) {
                dispatchHideTimeout();
            }
        }
    };

    private boolean mShown;
    private boolean mClosed;
    private long mShowTime;
    private long mHideTime;

    private long mHideTimeout = HIDE_TIMEOUT_DEFAULT;

    /** @see #setHideTimeout(long) */
    public final long getHideTimeout() {
        return mHideTimeout;
    }

    /**
     * Set the number of milliseconds before {@link #onHideTimeout()} is called after being hidden. This feature is
     * disabled by default. A negative value or {@link #NEVER} disables the hide timeout callback.
     * @param hideTimeout The hide timeout in milliseconds.
     */
    public final void setHideTimeout(long hideTimeout) {
        mHideTimeout = hideTimeout;
    }

    //region Observer Registration
    @Override
    public void registerDataObserver(@NonNull DataObserver dataObserver) {
        mDataObservers.register(dataObserver);
    }

    @Override
    public void unregisterDataObserver(@NonNull DataObserver dataObserver) {
        mDataObservers.unregister(dataObserver);
    }

    @Override
    public void registerAvailableObserver(@NonNull AvailableObserver availableObserver) {
        mAvailableObservers.register(availableObserver);
    }

    @Override
    public void unregisterAvailableObserver(@NonNull AvailableObserver availableObserver) {
        mAvailableObservers.unregister(availableObserver);
    }

    @Override
    public void registerLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        mLoadingObservers.register(loadingObserver);
    }

    @Override
    public void unregisterLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        mLoadingObservers.unregister(loadingObserver);
    }

    @Override
    public void registerErrorObserver(@NonNull ErrorObserver errorObserver) {
        mErrorObservers.register(errorObserver);
    }

    @Override
    public void unregisterErrorObserver(@NonNull ErrorObserver errorObserver) {
        mErrorObservers.unregister(errorObserver);
    }
    //endregion

    @NonNull
    @Override
    public final T get(int position) {
        return get(position, 0);
    }

    /**
     * Returns {@link #UNKNOWN} by default.
     * @see Data#available()
     */
    @Override
    public int available() {
        return UNKNOWN;
    }

    @Override
    public boolean isEmpty() {
        return size() <= 0;
    }

    /** Subclasses overriding this method should always make super call. */
    @Override
    public void notifyShown() {
        if (!mShown) {
            mShowTime = elapsedRealtime();
            mShown = true;
            dispatchShown(elapsedRealtime() - mHideTime);
            mHandler.removeCallbacks(mHideTimeoutRunnable);
        }
    }

    /** Subclasses overriding this method should always make super call. */
    @Override
    public void notifyHidden() {
        if (mShown) {
            mHideTime = elapsedRealtime();
            mShown = false;
            dispatchHidden(elapsedRealtime() - mShowTime);
            mHandler.removeCallbacks(mHideTimeoutRunnable);
            if (mHideTimeout >= 0 && !mClosed) {
                mHandler.postDelayed(mHideTimeoutRunnable, mHideTimeout);
            }
        }
    }

    /** Subclasses overriding this method should always make super call. */
    @Override
    public void close() {
        if (!mClosed) {
            mHandler.removeCallbacks(mHideTimeoutRunnable);
            mClosed = true;
            dispatchClose();
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new DataIterator<>(this);
    }

    /**
     * Called when this instance is closed. Only one invocation is ever made per-instance. Any exceptions are caught by
     * the caller. Subclasses must call through to super.
     * @throws Throwable If any error occurs. These exceptions are caught by the caller and logged.
     */
    protected void onClose() throws Throwable {
    }

    /**
     * Indicates if this instance is in a shown state, ie, {@link #notifyShown()} was called without any subsequent
     * {@link #notifyHidden()} call.
     * @return {@code true} is this data instance is in the shown state.
     */
    protected boolean isShown() {
        return mShown;
    }

    /** Dispatch a data change notification on the UI thread. */
    protected void notifyDataChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataObservers.notifyDataChanged();
            }
        });
    }

    protected void notifyItemChanged(final int position) {
        notifyItemRangeChanged(position, 1);
    }

    protected void notifyItemRangeChanged(final int positionStart, final int itemCount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataObservers.notifyItemRangeChanged(positionStart, itemCount);
            }
        });
    }

    protected void notifyItemInserted(int position) {
        notifyItemRangeInserted(position, 1);
    }

    protected void notifyItemRangeInserted(final int positionStart, final int itemCount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataObservers.notifyItemRangeInserted(positionStart, itemCount);
            }
        });
    }

    protected void notifyItemMoved(int fromPosition, int toPosition) {
        notifyItemRangeMoved(fromPosition, toPosition, 1);
    }

    protected void notifyItemRangeMoved(final int fromPosition, final int toPosition, final int itemCount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataObservers.notifyItemRangeMoved(fromPosition, toPosition, itemCount);
            }
        });
    }

    protected void notifyItemRemoved(int position) {
        notifyItemRangeRemoved(position, 1);
    }

    protected void notifyItemRangeRemoved(final int positionStart, final int itemCount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataObservers.notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
    }

    /** Dispatch a available change notification on the UI thread. */
    protected void notifyAvailableChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAvailableObservers.notifyAvailableChanged();
            }
        });
    }

    /** Dispatch a loading change notification on the UI thread. */
    protected void notifyLoadingChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoadingObservers.notifyLoadingChanged();
            }
        });
    }

    /** Dispatch an error notification on the UI thread. */
    protected void notifyError(@NonNull final Throwable e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mErrorObservers.notifyError(e);
            }
        });
    }

    /**
     * Called when the data enters the shown state. Never called twice without an intervening {@link #onHidden(long)}
     * call.
     */
    protected void onShown(long millisHidden) {
    }

    /**
     * Called when the data enters the hidden state. Never called twice without an intervening {@link #onShown(long)}
     * call.
     */
    protected void onHidden(long millisShown) {
    }

    /**
     * Called when the hide timeout duration has elapsed.
     * @see #setHideTimeout(long)
     */
    protected void onHideTimeout() {
    }

    /** Runs a task on the UI thread. If caller thread is the UI thread, the task is executed immediately. */
    protected void runOnUiThread(@NonNull Runnable runnable) {
        if (Looper.myLooper() == mHandler.getLooper()) {
            runnable.run();
        } else {
            mPoster.post(runnable);
        }
    }

    private void dispatchShown(long millisHidden) {
        try {
            onShown(millisHidden);
        } catch (Throwable e) {
            log.error("Error during shown callback", e);
        }
    }

    private void dispatchHidden(long millisShown) {
        try {
            onHidden(millisShown);
        } catch (Throwable e) {
            log.error("Error during hidden callback", e);
        }
    }

    private void dispatchHideTimeout() {
        try {
            onHideTimeout();
        } catch (Throwable e) {
            log.error("Error during hide timeout callback", e);
        }
    }

    private void dispatchClose() {
        try {
            onClose();
        } catch (Throwable e) {
            log.error("Error during close callback", e);
        }
    }
}
