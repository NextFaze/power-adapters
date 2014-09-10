package com.nextfaze.databind;

import android.os.Handler;
import lombok.NonNull;

import static android.os.Looper.getMainLooper;

class DataHelper {

    private static final long HIDE_TIMEOUT_DELAY = 3000;

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

    private boolean mLoading;
    private boolean mShown;

    public final void registerDataObserver(@NonNull DataObserver dataObserver) {
        mDataObservers.register(dataObserver);
    }

    public final void unregisterDataObserver(@NonNull DataObserver dataObserver) {
        mDataObservers.unregister(dataObserver);
    }

    public final void registerLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        mLoadingObservers.register(loadingObserver);
    }

    public final void unregisterLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        mLoadingObservers.unregister(loadingObserver);
    }

    public final void registerErrorObserver(@NonNull ErrorObserver errorObserver) {
        mErrorObservers.register(errorObserver);
    }

    public final void unregisterErrorObserver(@NonNull ErrorObserver errorObserver) {
        mErrorObservers.unregister(errorObserver);
    }

    public final void notifyShown() {
        if (!mShown) {
            mShown = true;
            onShown();
            mHandler.removeCallbacks(mHideTimeoutRunnable);
        }
    }

    public final void notifyHidden() {
        if (mShown) {
            mShown = false;
            onHidden();
            mHandler.removeCallbacks(mHideTimeoutRunnable);
            mHandler.postDelayed(mHideTimeoutRunnable, HIDE_TIMEOUT_DELAY);
        }
    }

    final boolean isLoading() {
        return mLoading;
    }

    final void setLoading(boolean loading) {
        if (loading != mLoading) {
            mLoading = loading;
            notifyLoadingChanged();
        }
    }

    final boolean isShown() {
        return mShown;
    }

    final void notifyChanged() {
        mDataObservers.notifyDataChanged();
    }

    final void notifyInvalidated() {
        mDataObservers.notifyDataInvalidated();
    }

    final void notifyLoadingChanged() {
        mLoadingObservers.notifyLoadingChanged();
    }

    final void notifyError(@NonNull Throwable e) {
        mErrorObservers.notifyError(e);
    }

    void onShown() {
    }

    void onHidden() {
    }

    void onHideTimeout() {
    }
}
