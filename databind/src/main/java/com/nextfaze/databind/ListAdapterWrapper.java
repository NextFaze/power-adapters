package com.nextfaze.databind;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import lombok.NonNull;

public class ListAdapterWrapper extends BaseAdapter implements DisposableListAdapter {

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

    private final boolean mDisposeWrappedAdapter;

    public ListAdapterWrapper(@NonNull ListAdapter adapter) {
        this(adapter, true);
    }

    public ListAdapterWrapper(@NonNull ListAdapter adapter, boolean disposeWrappedAdapter) {
        mAdapter = adapter;
        mDisposeWrappedAdapter = disposeWrappedAdapter;
        mAdapter.registerDataSetObserver(mDataSetObserver);
    }

    @Override
    public void dispose() {
        mAdapter.unregisterDataSetObserver(mDataSetObserver);
        if (mDisposeWrappedAdapter && mAdapter instanceof DisposableListAdapter) {
            ((DisposableListAdapter) mAdapter).dispose();
        }
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
}
