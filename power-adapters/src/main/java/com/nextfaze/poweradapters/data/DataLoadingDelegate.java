package com.nextfaze.poweradapters.data;

import com.nextfaze.asyncdata.Data;
import com.nextfaze.poweradapters.LoadingAdapter;
import lombok.NonNull;

public final class DataLoadingDelegate extends LoadingAdapter.Delegate {

    @NonNull
    private final Data<?> mData;

    @NonNull
    private final com.nextfaze.asyncdata.LoadingObserver mLoadingObserver = new com.nextfaze.asyncdata.LoadingObserver() {
        @Override
        public void onLoadingChange() {
            notifyLoadingChanged();
        }
    };

    public DataLoadingDelegate(@NonNull Data<?> data) {
        mData = data;
    }

    @Override
    protected boolean isLoading() {
        return mData.isLoading();
    }

    protected void onFirstObserverRegistered() {
        mData.registerLoadingObserver(mLoadingObserver);
    }

    protected void onLastObserverUnregistered() {
        mData.unregisterLoadingObserver(mLoadingObserver);
    }
}
