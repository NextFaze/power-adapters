package com.nextfaze.poweradapters.asyncdata;

import com.nextfaze.asyncdata.Data;
import com.nextfaze.poweradapters.LoadingAdapterBuilder;
import lombok.NonNull;

public final class DataLoadingDelegate extends LoadingAdapterBuilder.Delegate {

    @NonNull
    private final Data<?> mData;

    @NonNull
    private final com.nextfaze.asyncdata.DataObserver mDataObserver = new com.nextfaze.asyncdata.SimpleDataObserver() {
        @Override
        public void onChange() {
            notifyEmptyChanged();
        }
    };

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

    @Override
    protected boolean isEmpty() {
        return mData.isEmpty();
    }

    @Override
    protected void onFirstObserverRegistered() {
        mData.registerLoadingObserver(mLoadingObserver);
        mData.registerDataObserver(mDataObserver);
    }

    @Override
    protected void onLastObserverUnregistered() {
        mData.unregisterLoadingObserver(mLoadingObserver);
        mData.unregisterDataObserver(mDataObserver);
    }
}
