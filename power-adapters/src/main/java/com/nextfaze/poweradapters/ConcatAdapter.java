package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static java.lang.String.format;

/** Concatenates several adapters together. */
final class ConcatAdapter extends AbstractPowerAdapter {

    /** Reused to wrap an adapter and automatically offset all position calls. Not thread-safe obviously. */
    @NonNull
    private final OffsetAdapter mOffsetAdapter = new OffsetAdapter();

    @NonNull
    private final ArrayList<Entry> mEntries;

    @NonNull
    private final Map<ViewType, Entry> mEntriesByViewType = new HashMap<>();

    ConcatAdapter(@NonNull PowerAdapter... adapters) {
        mEntries = new ArrayList<>(adapters.length);
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < adapters.length; i++) {
            mEntries.add(new Entry(adapters[i]));
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

    private static final class OffsetAdapter {

        @NonNull
        private final WeakHashMap<View, OffsetAdapter.OffsetHolder> mHolders = new WeakHashMap<>();

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
