package com.nextfaze.poweradapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import lombok.NonNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

final class ConverterAdapter extends BaseAdapter {

    @NonNull
    private final Set<DataSetObserver> mDataSetObservers = new CopyOnWriteArraySet<>();

    @NonNull
    private final DataObserver mDataSetObserver = new DataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyDataSetChanged();
        }
    };

    @NonNull
    private final PowerAdapter mPowerAdapter;

    ConverterAdapter(@NonNull PowerAdapter powerAdapter) {
        mPowerAdapter = powerAdapter;
    }

    @Override
    public final Object getItem(int position) {
        return null;
    }

    @Override
    public final int getCount() {
        return mPowerAdapter.getItemCount();
    }

    @Override
    public final long getItemId(int position) {
        return mPowerAdapter.getItemId(position);
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mPowerAdapter.newView(parent, getItemViewType(position));
        }
        mPowerAdapter.bindView(convertView, position);
        return convertView;
    }

    @Override
    public final boolean hasStableIds() {
        return mPowerAdapter.hasStableIds();
    }

    @Override
    public final int getItemViewType(int position) {
        return mPowerAdapter.getItemViewType(position);
    }

    @Override
    public final int getViewTypeCount() {
        return mPowerAdapter.getViewTypeCount();
    }

    @Override
    public final void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
        if (mDataSetObservers.add(observer) && mDataSetObservers.size() == 1) {
            mPowerAdapter.registerDataObserver(mDataSetObserver);
        }
    }

    @Override
    public final void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
        if (mDataSetObservers.remove(observer) && mDataSetObservers.size() == 0) {
            mPowerAdapter.unregisterDataObserver(mDataSetObserver);
        }
    }
}
