package com.nextfaze.databind;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static android.os.Looper.getMainLooper;

// TODO: Make thread-safe.
@Slf4j
@Accessors(prefix = "m")
public abstract class AbstractData<T> implements Data<T> {

    private static final long HIDE_TIMEOUT_DEFAULT = 3 * 1000;

    @NonNull
    private final DataObservers mDataObservers = new DataObservers();

    @NonNull
    private final LoadingObservers mLoadingObservers = new LoadingObservers();

    @NonNull
    private final ErrorObservers mErrorObservers = new ErrorObservers();

    @NonNull
    private final Handler mHandler = new Handler(getMainLooper());

    @NonNull
    private final Runnable mHideTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            onHideTimeout();
        }
    };

    private boolean mShown;
    private long mShowTime;
    private long mHideTime;

    @Getter
    @Setter
    private long mHideTimeout = HIDE_TIMEOUT_DEFAULT;

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

    @Override
    public boolean isEmpty() {
        return size() <= 0;
    }

    @Override
    public void notifyShown() {
        if (!mShown) {
            mShowTime = SystemClock.elapsedRealtime();
            mShown = true;
            onShown(SystemClock.elapsedRealtime() - mHideTime);
            mHandler.removeCallbacks(mHideTimeoutRunnable);
        }
    }

    @Override
    public void notifyHidden() {
        if (mShown) {
            mHideTime = SystemClock.elapsedRealtime();
            mShown = false;
            onHidden(SystemClock.elapsedRealtime() - mShowTime);
            mHandler.removeCallbacks(mHideTimeoutRunnable);
            mHandler.postDelayed(mHideTimeoutRunnable, mHideTimeout);
        }
    }

    @Override
    public void close() {
        try {
            onClose();
        } catch (Exception e) {
            log.error("Error closing data", e);
        }
    }

    protected void onClose() throws Exception {
    }

    protected boolean isShown() {
        return mShown;
    }

    protected void notifyChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataObservers.notifyDataChanged();
            }
        });
    }

    protected void notifyInvalidated() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataObservers.notifyDataInvalidated();
            }
        });
    }

    protected void notifyLoadingChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoadingObservers.notifyLoadingChanged();
            }
        });
    }

    protected void notifyError(@NonNull final Throwable e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mErrorObservers.notifyError(e);
            }
        });
    }

    protected void onShown(long millisHidden) {
    }

    protected void onHidden(long millisShown) {
    }

    protected void onHideTimeout() {
    }

    protected void runOnUiThread(@NonNull Runnable runnable) {
        if (Looper.myLooper() == mHandler.getLooper()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
    }
}
