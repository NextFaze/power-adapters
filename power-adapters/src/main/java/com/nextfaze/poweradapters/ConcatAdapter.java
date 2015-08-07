package com.nextfaze.poweradapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.lang.String.format;

@Slf4j
@Accessors(prefix = "m")
public final class ConcatAdapter extends BaseAdapter {

    @NonNull
    private final Set<DataSetObserver> mDataSetObservers = new CopyOnWriteArraySet<>();

    @NonNull
    private final ArrayList<ListAdapter> mAdapters = new ArrayList<>();

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

    public ConcatAdapter(@NonNull ListAdapter... adapters) {
        this(Arrays.asList(adapters));
    }

    public ConcatAdapter(@NonNull Iterable<? extends ListAdapter> adapters) {
        for (ListAdapter adapter : adapters) {
            mAdapters.add(adapter);
        }
    }

    @Override
    public int getCount() {
        int count = 0;
        for (int i = 0; i < mAdapters.size(); i++) {
            count += mAdapters.get(i).getCount();
        }
        return count;
    }

    @Override
    public int getViewTypeCount() {
        int viewTypeCount = 0;
        for (int i = 0; i < mAdapters.size(); i++) {
            viewTypeCount += mAdapters.get(i).getViewTypeCount();
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

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
        if (mDataSetObservers.add(observer) && mDataSetObservers.size() == 1) {
            registerObserverWithChildren();
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
        if (mDataSetObservers.remove(observer) && mDataSetObservers.size() == 0) {
            unregisterObserverWithChildren();
        }
    }

    private void registerObserverWithChildren() {
        for (int i = 0; i < mAdapters.size(); i++) {
            mAdapters.get(i).registerDataSetObserver(mDataSetObserver);
        }
    }

    private void unregisterObserverWithChildren() {
        for (int i = 0; i < mAdapters.size(); i++) {
            mAdapters.get(i).unregisterDataSetObserver(mDataSetObserver);
        }
    }

    @NonNull
    private ListAdapter adapter(int position) {
        if (position >= getCount()) {
            throw new ArrayIndexOutOfBoundsException("index: " + position + ", total size: " + getCount());
        }
        int positionOffset = 0;
        int itemViewTypeOffset = 0;
        for (int i = 0; i < mAdapters.size(); i++) {
            ListAdapter adapter = mAdapters.get(i);
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

    private static final class OffsetAdapter implements ListAdapter {

        private ListAdapter mAdapter;

        private int mPositionOffset;
        private int mItemViewTypeOffset;

        @NonNull
        OffsetAdapter set(@NonNull ListAdapter adapter, int positionOffset, int itemViewTypeOffset) {
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
