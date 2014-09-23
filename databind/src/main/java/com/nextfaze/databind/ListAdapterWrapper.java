package com.nextfaze.databind;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import lombok.NonNull;

public class ListAdapterWrapper implements ListAdapter {

    @NonNull
    protected final ListAdapter mAdapter;

    public ListAdapterWrapper(@NonNull ListAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public int getCount() {
        return mAdapter.getCount();
    }

    @Override
    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return mAdapter.isEnabled(position);
    }

    @Override
    public void registerDataSetObserver(@NonNull DataSetObserver observer) {
        mAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(@NonNull DataSetObserver observer) {
        mAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public Object getItem(int position) {
        return mAdapter.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(position);
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mAdapter.getView(position, convertView, parent);
    }

    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return mAdapter.getViewTypeCount();
    }

    public void notifyDataSetChanged() {
        if (mAdapter instanceof BaseAdapter) {
            ((BaseAdapter) mAdapter).notifyDataSetChanged();
        }
    }

    public void notifyDataSetInvalidated() {
        if (mAdapter instanceof BaseAdapter) {
            ((BaseAdapter) mAdapter).notifyDataSetInvalidated();
        }
    }
}
