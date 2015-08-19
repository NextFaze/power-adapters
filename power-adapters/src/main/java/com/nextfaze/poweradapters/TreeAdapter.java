package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static java.lang.String.format;

public abstract class TreeAdapter extends AbstractPowerAdapter {

    @NonNull
    private final DataObserver mRootDataObserver = new DataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    };

    @NonNull
    private final PowerAdapter mRootAdapter;

    /** Reused to wrap an adapter and automatically offset all position calls. Not thread-safe obviously. */
    @NonNull
    private final OffsetAdapter mOffsetAdapter = new OffsetAdapter();

    @NonNull
    private final SparseArray<Entry> mEntries = new SparseArray<>();

    @NonNull
    private final Map<ViewType, Entry> mEntriesByViewType = new HashMap<>();

    public TreeAdapter(@NonNull PowerAdapter rootAdapter) {
        mRootAdapter = rootAdapter;
    }

    public void setExpanded(int position, boolean expanded) {
        Entry entry = mEntries.get(position);
        if (expanded) {
            if (entry == null) {
                entry = new Entry(getChildAdapter(position));
                mEntries.put(position, entry);
                notifyItemRangeInserted(position, entry.getItemCount());
            }
        } else {
            if (entry != null) {
                int itemCount = entry.getItemCount();
                entry.dispose();
                mEntries.remove(position);
                notifyItemRangeRemoved(position, itemCount);
            }
        }
    }

    public boolean isExpanded(int position) {
        return mEntries.get(position) != null;
    }

    @NonNull
    protected abstract PowerAdapter getChildAdapter(int position);

    @Override
    public boolean hasStableIds() {
        // We don't know all our adapters ahead of time, so we can never truly have stable IDs.
        return false;
    }

    @Override
    public int getItemCount() {
        // Account for root adapter items, at a minimum.
        int itemCount = mRootAdapter.getItemCount();
        // Add each expanded adapter item count also.
        for (int i = 0; i < mEntries.size(); i++) {
            itemCount += mEntries.get(i).getItemCount();
        }
        return itemCount;
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
    public ViewType getItemViewType(int position) {
        OffsetAdapter offsetAdapter = findAdapterByPosition(position);
        Entry entry = offsetAdapter.mEntry;
        ViewType viewType = offsetAdapter.getViewType(position);
        mEntriesByViewType.put(viewType, entry);
        return viewType;
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        return findAdapterByItemViewType(viewType).newView(parent, viewType);
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        findAdapterByPosition(holder.getPosition()).bindView(view, holder);
    }

    @CallSuper
    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mRootAdapter.registerDataObserver(mRootDataObserver);
        for (int i = 0; i < mEntries.size(); i++) {
            mEntries.get(i).registerObserversIfNecessary();
        }
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mRootAdapter.unregisterDataObserver(mRootDataObserver);
        for (int i = 0; i < mEntries.size(); i++) {
            mEntries.get(i).unregisterObserversIfNecessary();
        }
    }

    @NonNull
    private OffsetAdapter findAdapterByPosition(int position) {
        int totalItemCount = getItemCount();
        if (position >= totalItemCount) {
            throw new ArrayIndexOutOfBoundsException(format("Index: %d, total size: %d", position, totalItemCount));
        }
        int positionOffset = 0;
        for (int i = 0; i < mEntries.size(); i++) {
            Entry entry = mEntries.get(i);
            PowerAdapter adapter = entry.mAdapter;
            int itemCount = adapter.getItemCount();
            if (position - positionOffset < itemCount) {
                return mOffsetAdapter.set(entry, positionOffset);
            }
            positionOffset += itemCount;
        }
        throw new IndexOutOfBoundsException(
                format("Position %d not within range of any of the %d child adapters, total size %d",
                        position, mEntries.size(), totalItemCount));
    }

    @NonNull
    private OffsetAdapter findAdapterByItemViewType(@NonNull ViewType viewType) {
        Entry entryWithViewType = mEntriesByViewType.get(viewType);
        if (entryWithViewType != null) {
            int positionOffset = 0;
            for (int i = 0; i < mEntries.size(); i++) {
                Entry entry = mEntries.get(i);
                PowerAdapter adapter = entry.mAdapter;
                int itemCount = adapter.getItemCount();
                if (entry == entryWithViewType) {
                    return mOffsetAdapter.set(entry, positionOffset);
                }
                positionOffset += itemCount;
            }
        }
        throw new IllegalStateException("No entry found with the specified view type");
    }

    @NonNull
    private Entry findEntryByPosition(int position) {
        int totalItemCount = getItemCount();
        if (position >= totalItemCount) {
            throw new ArrayIndexOutOfBoundsException(format("Index: %d, total size: %d", position, totalItemCount));
        }
        int positionOffset = 0;
        for (int i = 0; i < mEntries.size(); i++) {
            Entry entry = mEntries.get(i);
            PowerAdapter adapter = entry.mAdapter;
            int itemCount = adapter.getItemCount();
            if (position - positionOffset < itemCount) {
                return entry;
            }
            positionOffset += itemCount;
        }
        throw new IndexOutOfBoundsException(
                format("Position %d not within range of any of the %d child adapters, total size %d",
                        position, mEntries.size(), totalItemCount));
    }

    private final class Entry {

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

        @Nullable
        private DataObserver mRegisteredDataObserver;

        @NonNull
        private final PowerAdapter mAdapter;

        Entry(@NonNull PowerAdapter adapter) {
            mAdapter = adapter;
            registerObserversIfNecessary();
        }

        private int innerToOuter(int innerPosition) {
            int positionOffset = 0;
            for (int i = 0; i < mEntries.size(); i++) {
                Entry entry = mEntries.get(i);
                if (entry.mAdapter == mAdapter) {
                    break;
                }
                positionOffset += entry.getItemCount();
            }
            return positionOffset + innerPosition;
        }

        void registerObserversIfNecessary() {
            if (mRegisteredDataObserver == null) {
                mAdapter.registerDataObserver(mDataObserver);
                mRegisteredDataObserver = mDataObserver;
            }
        }

        void unregisterObserversIfNecessary() {
            if (mRegisteredDataObserver != null) {
                mAdapter.unregisterDataObserver(mRegisteredDataObserver);
                mRegisteredDataObserver = null;
            }
        }

        int getItemCount() {
            return mAdapter.getItemCount();
        }

        void dispose() {
            unregisterObserversIfNecessary();
        }
    }

    private static final class OffsetAdapter {

        @NonNull
        private final WeakHashMap<View, OffsetHolder> mHolders = new WeakHashMap<>();

        private Entry mEntry;

        private int mPositionOffset;

        @NonNull
        OffsetAdapter set(@NonNull Entry entry, int positionOffset) {
            mEntry = entry;
            mPositionOffset = positionOffset;
            return this;
        }

        long getItemId(int position) {
            return mEntry.mAdapter.getItemId(position - mPositionOffset);
        }

        boolean isEnabled(int position) {
            return mEntry.mAdapter.isEnabled(position - mPositionOffset);
        }

        @NonNull
        View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
            return mEntry.mAdapter.newView(parent, viewType);
        }

        void bindView(@NonNull View view, @NonNull Holder holder) {
            OffsetHolder offsetHolder = mHolders.get(view);
            if (offsetHolder == null) {
                offsetHolder = new OffsetHolder(holder);
                mHolders.put(view, offsetHolder);
            }
            offsetHolder.offset = mPositionOffset;
            mEntry.mAdapter.bindView(view, offsetHolder);
        }

        @NonNull
        ViewType getViewType(int position) {
            return mEntry.mAdapter.getItemViewType(position - mPositionOffset);
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
}
