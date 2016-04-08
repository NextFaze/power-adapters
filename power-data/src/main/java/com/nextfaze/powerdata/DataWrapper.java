package com.nextfaze.powerdata;

import lombok.NonNull;

public abstract class DataWrapper<T> extends AbstractData<T> {

    @NonNull
    private final Data<?> mData;

    @NonNull
    private final DataObserver mDataObserver = new DataObserver() {
        @Override
        public void onChange() {
            forwardChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            forwardItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            forwardItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            forwardItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            forwardItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    };

    @NonNull
    private final LoadingObserver mLoadingObserver = new LoadingObserver() {
        @Override
        public void onLoadingChange() {
            forwardLoadingChanged();
        }
    };

    @NonNull
    private final AvailableObserver mAvailableObserver = new AvailableObserver() {
        @Override
        public void onAvailableChange() {
            forwardAvailableChanged();
        }
    };

    @NonNull
    private final ErrorObserver mErrorObserver = new ErrorObserver() {
        @Override
        public void onError(@NonNull Throwable e) {
            forwardError(e);
        }
    };

    private boolean mObservingData;
    private boolean mObservingLoading;
    private boolean mObservingError;
    private boolean mObservingAvailable;

    public DataWrapper(@NonNull Data<?> data) {
        mData = data;
    }

    @Override
    public void invalidate() {
        mData.invalidate();
    }

    @Override
    public void refresh() {
        mData.refresh();
    }

    @Override
    public void reload() {
        mData.reload();
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
    public int available() {
        return mData.available();
    }

    @Override
    public boolean isEmpty() {
        return mData.isEmpty();
    }

    @Override
    public void registerDataObserver(@NonNull DataObserver dataObserver) {
        super.registerDataObserver(dataObserver);
        updateDataObserver();
    }

    @Override
    public void unregisterDataObserver(@NonNull DataObserver dataObserver) {
        super.unregisterDataObserver(dataObserver);
        updateDataObserver();
    }

    @Override
    public void registerAvailableObserver(@NonNull AvailableObserver availableObserver) {
        super.registerAvailableObserver(availableObserver);
        updateAvailableObserver();
    }

    @Override
    public void unregisterAvailableObserver(@NonNull AvailableObserver availableObserver) {
        super.unregisterAvailableObserver(availableObserver);
        updateAvailableObserver();
    }

    @Override
    public void registerLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        super.registerLoadingObserver(loadingObserver);
        updateLoadingObserver();
    }

    @Override
    public void unregisterLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        super.unregisterLoadingObserver(loadingObserver);
        updateLoadingObserver();
    }

    @Override
    public void registerErrorObserver(@NonNull ErrorObserver errorObserver) {
        super.registerErrorObserver(errorObserver);
        updateErrorObserver();
    }

    @Override
    public void unregisterErrorObserver(@NonNull ErrorObserver errorObserver) {
        super.unregisterErrorObserver(errorObserver);
        updateErrorObserver();
    }

    protected void forwardChanged() {
        notifyDataChanged();
    }

    protected void forwardItemRangeChanged(int innerPositionStart, int innerItemCount) {
        notifyItemRangeChanged(innerToOuter(innerPositionStart), innerItemCount);
    }

    protected void forwardItemRangeInserted(int innerPositionStart, int innerItemCount) {
        notifyItemRangeInserted(innerToOuter(innerPositionStart), innerItemCount);
    }

    protected void forwardItemRangeRemoved(int innerPositionStart, int innerItemCount) {
        notifyItemRangeRemoved(innerToOuter(innerPositionStart), innerItemCount);
    }

    protected void forwardItemRangeMoved(int innerFromPosition, int innerToPosition, int innerItemCount) {
        notifyItemRangeMoved(innerToOuter(innerFromPosition), innerToOuter(innerToPosition), innerItemCount);
    }

    protected void forwardLoadingChanged() {
        notifyLoadingChanged();
    }

    protected void forwardError(@NonNull Throwable e) {
        notifyError(e);
    }

    protected void forwardAvailableChanged() {
        notifyAvailableChanged();
    }

    protected int outerToInner(int outerPosition) {
        return outerPosition;
    }

    protected int innerToOuter(int innerPosition) {
        return innerPosition;
    }

    private void updateDataObserver() {
        if (mObservingData && getDataObserverCount() <= 0) {
            mData.unregisterDataObserver(mDataObserver);
            mObservingData = false;
        } else if (!mObservingData && getDataObserverCount() > 0) {
            mData.registerDataObserver(mDataObserver);
            mObservingData = true;
        }
    }

    private void updateLoadingObserver() {
        if (mObservingLoading && getLoadingObserverCount() <= 0) {
            mData.unregisterLoadingObserver(mLoadingObserver);
            mObservingLoading = false;
        } else if (!mObservingLoading && getLoadingObserverCount() > 0) {
            mData.registerLoadingObserver(mLoadingObserver);
            mObservingLoading = true;
        }
    }

    private void updateAvailableObserver() {
        if (mObservingAvailable && getAvailableObserverCount() <= 0) {
            mData.unregisterAvailableObserver(mAvailableObserver);
            mObservingAvailable = false;
        } else if (!mObservingAvailable && getAvailableObserverCount() > 0) {
            mData.registerAvailableObserver(mAvailableObserver);
            mObservingAvailable = true;
        }
    }

    private void updateErrorObserver() {
        if (mObservingError && getErrorObserverCount() <= 0) {
            mData.unregisterErrorObserver(mErrorObserver);
            mObservingError = false;
        } else if (!mObservingError && getErrorObserverCount() > 0) {
            mData.registerErrorObserver(mErrorObserver);
            mObservingError = true;
        }
    }
}

