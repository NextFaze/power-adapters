package com.nextfaze.powerdata;

import android.database.DataSetObserver;
import android.widget.BaseAdapter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

/** Presents the contents of a {@link Data} instance, and responds to change events. */
@Accessors(prefix = "m")
public abstract class DataAdapter<T> extends BaseAdapter {

    @NonNull
    private final Set<DataSetObserver> mDataObservers = new HashSet<>();

    @NonNull
    private final Data<T> mData;

    @NonNull
    private final DataObserver mDataObserver = new SimpleDataObserver() {
        @Override
        public void onChange() {
            notifyDataSetChanged();
        }
    };

    public DataAdapter(@NonNull Data<T> data) {
        mData = data;
    }

    @NonNull
    public final Data<T> getData() {
        return mData;
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
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
        if (mDataObservers.add(observer) && mDataObservers.size() == 1) {
            mData.registerDataObserver(mDataObserver);
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
        if (mDataObservers.remove(observer) && mDataObservers.size() == 0) {
            mData.unregisterDataObserver(mDataObserver);
        }
    }

    /** By default, simply returns the position, since a data item does not intrinsically have a {@code long} ID. */
    @Override
    public long getItemId(int position) {
        return position;
    }
}
