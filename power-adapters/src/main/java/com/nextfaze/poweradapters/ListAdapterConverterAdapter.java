package com.nextfaze.poweradapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

final class ListAdapterConverterAdapter extends BaseAdapter {

    @NonNull
    private final WeakHashMap<View, HolderImpl> mHolders = new WeakHashMap<>();

    @NonNull
    private final Set<DataSetObserver> mDataSetObservers = new HashSet<>();

    @NonNull
    private final DataObserver mDataObserver = new SimpleDataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }
    };

    @NonNull
    private final PowerAdapter mPowerAdapter;

    ListAdapterConverterAdapter(@NonNull PowerAdapter powerAdapter) {
        mPowerAdapter = powerAdapter;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return mPowerAdapter.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        return mPowerAdapter.getItemId(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        HolderImpl holder;
        if (convertView == null) {
            holder = new HolderImpl();
            convertView = mPowerAdapter.newView(parent, getItemViewType(position));
            mHolders.put(convertView, holder);
        } else {
            holder = mHolders.get(convertView);
        }
        holder.position = position;
        mPowerAdapter.bindView(convertView, holder);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return mPowerAdapter.hasStableIds();
    }

    @Override
    public int getItemViewType(int position) {
        return mPowerAdapter.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return mPowerAdapter.getViewTypeCount();
    }

    @Override
    public boolean isEnabled(int position) {
        return mPowerAdapter.isEnabled(position);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
        if (mDataSetObservers.add(observer) && mDataSetObservers.size() == 1) {
            mPowerAdapter.registerDataObserver(mDataObserver);
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
        if (mDataSetObservers.remove(observer) && mDataSetObservers.size() == 0) {
            mPowerAdapter.unregisterDataObserver(mDataObserver);
        }
    }

    private static final class HolderImpl implements Holder {

        int position;

        @Override
        public int getPosition() {
            return position;
        }
    }
}
