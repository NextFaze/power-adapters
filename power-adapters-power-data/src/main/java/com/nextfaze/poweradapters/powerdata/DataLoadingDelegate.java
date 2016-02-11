package com.nextfaze.poweradapters.powerdata;

import com.nextfaze.poweradapters.LoadingAdapterBuilder;
import com.nextfaze.powerdata.Data;
import lombok.NonNull;

public final class DataLoadingDelegate extends LoadingAdapterBuilder.Delegate {

    @NonNull
    private final Data<?> mData;

    @NonNull
    private final com.nextfaze.powerdata.DataObserver mDataObserver = new com.nextfaze.powerdata.SimpleDataObserver() {
        @Override
        public void onChange() {
            notifyEmptyChanged();
        }
    };

    @NonNull
    private final com.nextfaze.powerdata.LoadingObserver mLoadingObserver = new com.nextfaze.powerdata.LoadingObserver() {
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
