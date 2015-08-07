package com.nextfaze.asyncdata;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.String.format;

@Slf4j
@Accessors(prefix = "m")
public final class CompositeAdapter extends BaseAdapter implements DisposableListAdapter {

    @NonNull
    private final ArrayList<DisposableListAdapter> mAdapters = new ArrayList<>();

    /** Reused to wrap an adapter and automatically offset all position calls. Not thread-safe obviously. */
    @NonNull
    private final OffsetAdapter mOffsetAdapter = new OffsetAdapter();

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

    public CompositeAdapter(@NonNull DisposableListAdapter... adapters) {
        this(Arrays.asList(adapters));
    }

    public CompositeAdapter(@NonNull Iterable<? extends DisposableListAdapter> adapters) {
        for (DisposableListAdapter adapter : adapters) {
            mAdapters.add(adapter);
            adapter.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    public void dispose() {
        for (DisposableListAdapter adapter : mAdapters) {
            adapter.unregisterDataSetObserver(mDataSetObserver);
            adapter.dispose();
        }
    }

    @Override
    public int getCount() {
        int count = 0;
        for (DisposableListAdapter adapter : mAdapters) {
            count += adapter.getCount();
        }
        return count;
    }

    @Override
    public int getViewTypeCount() {
        int viewTypeCount = 0;
        for (DisposableListAdapter adapter : mAdapters) {
            viewTypeCount += adapter.getViewTypeCount();
        }
        return viewTypeCount;
    }

    @Override
    public Object getItem(int position) {
        return adapter(position).getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return adapter(position).getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return adapter(position).getView(position, convertView, parent);
    }

    @Override
    public boolean isEnabled(int position) {
        return adapter(position).isEnabled(position);
    }

    @Override
    public int getItemViewType(int position) {
        return adapter(position).getItemViewType(position);
    }

    @NonNull
    private DisposableListAdapter adapter(int position) {
        if (position >= getCount()) {
            throw new ArrayIndexOutOfBoundsException("index: " + position + ", total size: " + getCount());
        }
        int positionOffset = 0;
        int itemViewTypeOffset = 0;
        for (DisposableListAdapter adapter : mAdapters) {
            int count = adapter.getCount();
            if (position - positionOffset < count) {
                return mOffsetAdapter.set(adapter, positionOffset, itemViewTypeOffset);
            }
            positionOffset += count;
            itemViewTypeOffset += adapter.getViewTypeCount();
        }
        throw new IndexOutOfBoundsException(format("position %d not within range of any of the %d child adapters, total size %d",
                position, mAdapters.size(), getCount()));
    }

    private static final class OffsetAdapter implements DisposableListAdapter {

        @NonNull
        private DisposableListAdapter mAdapter;

        private int mPositionOffset;
        private int mItemViewTypeOffset;

        @NonNull
        OffsetAdapter set(@NonNull DisposableListAdapter adapter, int positionOffset, int itemViewTypeOffset) {
            mAdapter = adapter;
            mPositionOffset = positionOffset;
            mItemViewTypeOffset = itemViewTypeOffset;
            return this;
        }

        @Override
        public boolean isEnabled(int position) {
            return mAdapter.isEnabled(position - mPositionOffset);
        }

        @Override
        public Object getItem(int position) {
            return mAdapter.getItem(position - mPositionOffset);
        }

        @Override
        public long getItemId(int position) {
            return mAdapter.getItemId(position - mPositionOffset);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mAdapter.getView(position - mPositionOffset, convertView, parent);
        }

        @Override
        public int getItemViewType(int position) {
            return mAdapter.getItemViewType(position - mPositionOffset) + mItemViewTypeOffset;
        }

        @Override
        public void dispose() {
            mAdapter.dispose();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return mAdapter.areAllItemsEnabled();
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            mAdapter.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            mAdapter.unregisterDataSetObserver(observer);
        }

        @Override
        public int getCount() {
            return mAdapter.getCount();
        }

        @Override
        public boolean hasStableIds() {
            return mAdapter.hasStableIds();
        }

        @Override
        public int getViewTypeCount() {
            return mAdapter.getViewTypeCount();
        }

        @Override
        public boolean isEmpty() {
            return mAdapter.isEmpty();
        }
    }
}
