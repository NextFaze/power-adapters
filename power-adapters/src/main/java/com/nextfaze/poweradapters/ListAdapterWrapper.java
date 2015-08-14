package com.nextfaze.poweradapters;

import android.database.DataSetObserver;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;

public class ListAdapterWrapper extends BaseAdapter {

    @NonNull
    private final Set<DataSetObserver> mDataSetObservers = new HashSet<>();

    @NonNull
    protected final ListAdapter mAdapter;

    @NonNull
    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            notifyDataSetInvalidated();
        }
    };

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

    @Nullable
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

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
        if (mDataSetObservers.add(observer) && mDataSetObservers.size() == 1) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
            onFirstObserverRegistered();
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
        if (mDataSetObservers.remove(observer) && mDataSetObservers.size() == 0) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            onLastObserverUnregistered();
        }
    }

    /** Called when the first observer has registered with this adapter. */
    @CallSuper
    protected void onFirstObserverRegistered() {
    }

    /** Called when the last observer has unregistered from this adapter. */
    @CallSuper
    protected void onLastObserverUnregistered() {
    }
}
