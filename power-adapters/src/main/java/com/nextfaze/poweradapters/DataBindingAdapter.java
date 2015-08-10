package com.nextfaze.poweradapters;

import com.nextfaze.asyncdata.Data;
import lombok.NonNull;

public final class DataBindingAdapter extends BindingPowerAdapter {

    @NonNull
    private final com.nextfaze.asyncdata.DataObserver mDataObserver = new com.nextfaze.asyncdata.DataObserver() {
        @Override
        public void onChange() {
            notifyDataSetChanged();
        }
    };

    @NonNull
    private final Data<?> mData;

    public DataBindingAdapter(@NonNull Data<?> data, @NonNull Mapper mapper) {
        super(mapper);
        mData = data;
    }

    @NonNull
    @Override
    protected Object getItem(int position) {
        return mData.get(position, Data.FLAG_PRESENTATION);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mData.registerDataObserver(mDataObserver);
    }

    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mData.unregisterDataObserver(mDataObserver);
    }
}
