package com.nextfaze.databind;

import android.os.Handler;
import lombok.NonNull;

import static android.os.Looper.getMainLooper;

// TODO: Make thread-safe.
public abstract class AbstractData<T> implements Data<T> {

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
    public void notifyShown() {
        if (!mShown) {
            mShown = true;
            onShown();
            mHandler.removeCallbacks(mHideTimeoutRunnable);
        }
    }

    @Override
    public void notifyHidden() {
        if (mShown) {
            mShown = false;
            onHidden();
            mHandler.removeCallbacks(mHideTimeoutRunnable);
            mHandler.postDelayed(mHideTimeoutRunnable, HIDE_TIMEOUT_DELAY);
        }
    }

    @Override
    public boolean isLoading() {
        return mLoading;
    }

    protected void setLoading(boolean loading) {
        if (loading != mLoading) {
            mLoading = loading;
            notifyLoadingChanged();
        }
    }

    protected boolean isShown() {
        return mShown;
    }

    protected void notifyChanged() {
        mDataObservers.notifyDataChanged();
    }

    protected void notifyInvalidated() {
        mDataObservers.notifyDataInvalidated();
    }

    protected void notifyLoadingChanged() {
        mLoadingObservers.notifyLoadingChanged();
    }

    protected void notifyError(@NonNull Throwable e) {
        mErrorObservers.notifyError(e);
    }

    protected void onShown() {
    }

    protected void onHidden() {
    }

    protected void onHideTimeout() {
    }
}
