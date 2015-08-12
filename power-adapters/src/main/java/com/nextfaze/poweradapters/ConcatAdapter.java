package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.WeakHashMap;

import static java.lang.String.format;

/** Concatenates several adapters together. */
@Accessors(prefix = "m")
final class ConcatAdapter extends AbstractPowerAdapter {

    /** Reused to wrap an adapter and automatically offset all position calls. Not thread-safe obviously. */
    @NonNull
    private final OffsetAdapter mOffsetAdapter = new OffsetAdapter();

    @NonNull
    private final ArrayList<Entry> mEntries;

    ConcatAdapter(@NonNull PowerAdapter... adapters) {
        mEntries = new ArrayList<>(adapters.length);
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < adapters.length; i++) {
            PowerAdapter adapter = adapters[i];
            mEntries.add(new Entry(adapter));
        }
    }

    ConcatAdapter(@NonNull Iterable<? extends PowerAdapter> adapters) {
        mEntries = new ArrayList<>();
        for (PowerAdapter adapter : adapters) {
            mEntries.add(new Entry(adapter));
        }
    }

    @Override
    public boolean hasStableIds() {
        for (int i = 0; i < mEntries.size(); i++) {
            if (!mEntries.get(i).mAdapter.hasStableIds()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (int i = 0; i < mEntries.size(); i++) {
            count += mEntries.get(i).mAdapter.getItemCount();
        }
        return count;
    }

    @Override
    public int getViewTypeCount() {
        int viewTypeCount = 0;
        for (int i = 0; i < mEntries.size(); i++) {
            viewTypeCount += mEntries.get(i).mAdapter.getViewTypeCount();
        }
        return viewTypeCount;
    }

    @Override
    public long getItemId(int position) {
        return findAdapterByPosition(position).getItemId(position);
    }

    @Override
    public boolean isEnabled(int position) {
        return findAdapterByPosition(position).isEnabled(position);
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, int itemViewType) {
        return findAdapterByItemViewType(itemViewType).newView(parent, itemViewType);
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        findAdapterByPosition(holder.getPosition()).bindView(view, holder);
    }

    @Override
    public int getItemViewType(int position) {
        return findAdapterByPosition(position).getItemViewType(position);
    }

    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        for (int i = 0; i < mEntries.size(); i++) {
            mEntries.get(i).registerObservers();
        }
    }

    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        for (int i = 0; i < mEntries.size(); i++) {
            mEntries.get(i).unregisterObservers();
        }
    }

    @NonNull
    private OffsetAdapter findAdapterByPosition(int position) {
        int totalItemCount = getItemCount();
        if (position >= totalItemCount) {
            throw new ArrayIndexOutOfBoundsException(format("Index: %d, total size: %d", position, totalItemCount));
        }
        int positionOffset = 0;
        int itemViewTypeOffset = 0;
        for (int i = 0; i < mEntries.size(); i++) {
            PowerAdapter adapter = mEntries.get(i).mAdapter;
            int itemCount = adapter.getItemCount();
            if (position - positionOffset < itemCount) {
                return mOffsetAdapter.set(adapter, positionOffset, itemViewTypeOffset);
            }
            positionOffset += itemCount;
            itemViewTypeOffset += adapter.getViewTypeCount();
        }
        throw new IndexOutOfBoundsException(
                format("Position %d not within range of any of the %d child adapters, total size %d",
                        position, mEntries.size(), totalItemCount));
    }

    @NonNull
    private OffsetAdapter findAdapterByItemViewType(int itemViewType) {
        // TODO: Can be done better with a map.
        int totalViewTypeCount = getViewTypeCount();
        if (itemViewType >= totalViewTypeCount) {
            throw new ArrayIndexOutOfBoundsException(format("Item view type: %d, total item view types: %d",
                    itemViewType, totalViewTypeCount));
        }
        int positionOffset = 0;
        int itemViewTypeOffset = 0;
        for (int i = 0; i < mEntries.size(); i++) {
            PowerAdapter adapter = mEntries.get(i).mAdapter;
            int itemCount = adapter.getItemCount();
            int viewTypeCount = adapter.getViewTypeCount();
            if (itemViewType - itemViewTypeOffset < viewTypeCount) {
                return mOffsetAdapter.set(adapter, positionOffset, itemViewTypeOffset);
            }
            positionOffset += itemCount;
            itemViewTypeOffset += viewTypeCount;
        }
        throw new IndexOutOfBoundsException(
                format("Item view type %d not within range of any of the %d child adapters, total item view types %d",
                        itemViewType, mEntries.size(), totalViewTypeCount));
    }

    private static final class OffsetAdapter {

        @NonNull
        private final WeakHashMap<View, OffsetAdapter.OffsetHolder> mHolders = new WeakHashMap<>();

        private PowerAdapter mAdapter;

        private int mPositionOffset;
        private int mItemViewTypeOffset;

        @NonNull
        OffsetAdapter set(@NonNull PowerAdapter adapter, int positionOffset, int itemViewTypeOffset) {
            mAdapter = adapter;
            mPositionOffset = positionOffset;
            mItemViewTypeOffset = itemViewTypeOffset;
            return this;
        }

        long getItemId(int position) {
            return mAdapter.getItemId(position - mPositionOffset);
        }

        boolean isEnabled(int position) {
            return mAdapter.isEnabled(position - mPositionOffset);
        }

        @NonNull
        View newView(@NonNull ViewGroup parent, int itemViewType) {
            return mAdapter.newView(parent, itemViewType - mItemViewTypeOffset);
        }

        void bindView(@NonNull View view, @NonNull Holder holder) {
            OffsetHolder offsetHolder = mHolders.get(view);
            if (offsetHolder == null) {
                offsetHolder = new OffsetHolder(holder);
                mHolders.put(view, offsetHolder);
            }
            offsetHolder.offset = mPositionOffset;
            mAdapter.bindView(view, offsetHolder);
        }

        int getItemViewType(int position) {
            return mAdapter.getItemViewType(position - mPositionOffset) + mItemViewTypeOffset;
        }

        int getViewTypeCount() {
            return mAdapter.getViewTypeCount();
        }

        private static final class OffsetHolder extends HolderWrapper {

            int offset;

            OffsetHolder(@NonNull Holder holder) {
                super(holder);
            }

            @Override
            public int getPosition() {
                return super.getPosition() - offset;
            }
        }
    }

    private final class Entry {

        @NonNull
        private final PowerAdapter mAdapter;

        @NonNull
        private final DataObserver mDataObserver = new DataObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                notifyItemRangeChanged(innerToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                notifyItemRangeInserted(innerToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                notifyItemRangeRemoved(innerToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                notifyItemRangeMoved(innerToOuter(fromPosition), innerToOuter(toPosition), itemCount);
            }
        };

        private int innerToOuter(int innerPosition) {
            int positionOffset = 0;
            for (int i = 0; i < mEntries.size(); i++) {
                Entry entry = mEntries.get(i);
                if (entry.mAdapter == mAdapter) {
                    break;
                }
                positionOffset += entry.mAdapter.getItemCount();
            }
            return positionOffset + innerPosition;
        }

        Entry(@NonNull PowerAdapter adapter) {
            mAdapter = adapter;
        }

        void registerObservers() {
            mAdapter.registerDataObserver(mDataObserver);
        }

        void unregisterObservers() {
            mAdapter.unregisterDataObserver(mDataObserver);
        }
    }
}
