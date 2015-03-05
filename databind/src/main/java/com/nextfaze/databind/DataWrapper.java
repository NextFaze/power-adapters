package com.nextfaze.databind;

import lombok.NonNull;

public class DataWrapper<T> extends AbstractData<T> {

    @NonNull
    private final Data<T> mData;

    @NonNull
    private final DataObserver mDataObserver = new DataObserver() {
        @Override
        public void onChange() {
            notifyDataChanged();
        }
    };

    @NonNull
    private final LoadingObserver mLoadingObserver = new LoadingObserver() {
        @Override
        public void onLoadingChange() {
            notifyLoadingChanged();
        }
    };

    @NonNull
    private final ErrorObserver mErrorObserver = new ErrorObserver() {
        @Override
        public void onError(@NonNull Throwable e) {
            notifyError(e);
        }
    };

    private final boolean mCloseInnerData;

    public DataWrapper(@NonNull Data<T> data) {
        this(data, true);
    }

    public DataWrapper(@NonNull Data<T> data, boolean closeInnerData) {
        mData = data;
        mCloseInnerData = closeInnerData;
        mData.registerDataObserver(mDataObserver);
        mData.registerLoadingObserver(mLoadingObserver);
        mData.registerErrorObserver(mErrorObserver);
    }

    /** Subclasses must make super call. */
    @Override
    protected void onClose() throws Exception {
        mData.unregisterDataObserver(mDataObserver);
        mData.unregisterLoadingObserver(mLoadingObserver);
        mData.unregisterErrorObserver(mErrorObserver);
        if (mCloseInnerData) {
            mData.close();
        }
    }

    @NonNull
    @Override
    public T get(int position) {
        return mData.get(position);
    }

    @Override
    public int size() {
        return mData.size();
    }

    @Override
    public boolean isLoading() {
        return mData.isLoading();
    }

    @Override
    public boolean isEmpty() {
        return mData.isEmpty();
    }

    @Override
    public void notifyShown() {
        mData.notifyShown();
    }

    @Override
    public void notifyHidden() {
        mData.notifyHidden();
    }
}
