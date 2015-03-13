package com.nextfaze.databind;

import android.widget.BaseAdapter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
public abstract class DataAdapter<T> extends BaseAdapter implements DisposableListAdapter {

    @NonNull
    private final Data<T> mData;

    @NonNull
    private final DataObserver mDataObserver = new DataObserver() {
        @Override
        public void onChange() {
            notifyDataSetChanged();
        }
    };

    public DataAdapter(@NonNull Data<T> data) {
        mData = data;
        mData.registerDataObserver(mDataObserver);
    }

    @NonNull
    public final Data<T> getData() {
        return mData;
    }

    @Override
    public final void dispose() {
        mData.unregisterDataObserver(mDataObserver);
    }

    @Override
    public final int getCount() {
        return mData.size();
    }

    @Override
    public final T getItem(int position) {
        return mData.get(position, Data.FLAG_PRESENTATION);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
