package com.nextfaze.databind;

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

    protected void onClose() throws Throwable {
    }

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

    /** Called when the data enters the shown state. */
    protected void onShown(long millisHidden) {
    }

    /** Called when the data enters the hidden state. */
    protected void onHidden(long millisShown) {
    }

    /**
     * Called when the hide timeout duration has elapsed.
     * @see #setHideTimeout(long)
     */
    protected void onHideTimeout() {
    }

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
