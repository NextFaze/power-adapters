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

final class ConcatAdapter extends PowerAdapter {

    /** Reused to wrap an adapter and automatically offset all position calls. */
    @NonNull
    private final OffsetAdapter mOffsetAdapter = new OffsetAdapter();

    @NonNull
    private final SparseArray<Entry> mEntries = new SparseArray<>();

    @NonNull
    private final Map<ViewType, PowerAdapter> mAdaptersByViewType = new HashMap<>();

    /** Maps outer entry start positions to entries. */
    @NonNull
    private final SparseArray<Entry> mEntryMapping = new SparseArray<>();

    ConcatAdapter(@NonNull Iterable<? extends PowerAdapter> adapters) {
        int i = 0;
        for (PowerAdapter adapter : adapters) {
            mEntries.append(i, new Entry(adapter, i));
            i++;
        }
    }

    private void rebuild() {
        int outerStart = 0;
        mEntryMapping.clear();
        for (int i = 0; i < mEntries.size(); i++) {
            Entry entry = mEntries.valueAt(i);
            entry.mOuterStart = outerStart;
            int itemCount = entry.getItemCount();
            if (itemCount > 0) {
                mEntryMapping.put(entry.mOuterStart, entry);
            }
            outerStart += itemCount;
        }
    }

    @NonNull
    private Entry outerPositionToEntry(int outerPosition) {
        int entryIndex = mEntryMapping.indexOfKey(outerPosition);
        if (entryIndex >= 0) {
            return mEntryMapping.valueAt(entryIndex);
        }
        return mEntryMapping.valueAt(-entryIndex - 2);
    }

    @NonNull
    private OffsetAdapter outerToAdapter(int outerPosition) {
        Entry entry = outerPositionToEntry(outerPosition);
        if (entry.getItemCount() <= 0) {
            throw new AssertionError();
        }
        return mOffsetAdapter.set(entry.mAdapter, entry.mTransform, entry.getOuterStart());
    }

    private int outerToChild(int outerPosition) {
        Entry entry = outerPositionToEntry(outerPosition);
        return outerPosition - entry.getOuterStart();
    }

    @NonNull
    private PowerAdapter adapterForViewType(@NonNull ViewType viewType) {
        return mAdaptersByViewType.get(viewType);
    }

    @Override
    public int getItemCount() {
        if (mEntries.size() == 0) {
            return 0;
        }
        Entry entry = mEntries.valueAt(mEntries.size() - 1);
        return entry.getOuterStart() + entry.getItemCount();
    }

    @Override
    public boolean hasStableIds() {
        //noinspection SimplifiableIfStatement
        if (mEntries.size() == 1) {
            // Only a single entry, so it's safe to forward it's value directly.
            return mEntries.valueAt(0).mAdapter.hasStableIds();
        }
        // Otherwise, must return false because IDs returned by multiple
        // child adapters may collide, falsely indicating equality.
        return false;
    }

    @Override
    public long getItemId(int position) {
        return outerToAdapter(position).getItemId(position);
    }

    @Override
    public boolean isEnabled(int position) {
        return outerToAdapter(position).isEnabled(position);
    }

    @NonNull
    @Override
    public ViewType getItemViewType(int position) {
        OffsetAdapter offsetAdapter = outerToAdapter(position);
        ViewType viewType = offsetAdapter.getViewType(position);
        mAdaptersByViewType.put(viewType, offsetAdapter.mAdapter);
        return viewType;
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        return adapterForViewType(viewType).newView(parent, viewType);
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        outerToAdapter(holder.getPosition()).bindView(view, holder);
    }

    @CallSuper
    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        updateEntryObservers();
        // Rebuild index immediately. It will be updated incrementally later.
        rebuild();
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        updateEntryObservers();
    }

    private void updateEntryObservers() {
        for (int i = 0; i < mEntries.size(); i++) {
            mEntries.valueAt(i).updateObserver();
        }
    }

    private final class Entry {

        @NonNull
        private final DataObserver mDataObserver = new DataObserver() {
            @Override
            public void onChanged() {
                mShadowItemCount = mAdapter.getItemCount();
                rebuild();
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                notifyItemRangeChanged(childToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mShadowItemCount += itemCount;
                rebuild();
                notifyItemRangeInserted(childToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mShadowItemCount -= itemCount;
                rebuild();
                notifyItemRangeRemoved(childToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                notifyItemRangeMoved(childToOuter(fromPosition), childToOuter(toPosition), itemCount);
            }
        };

        @NonNull
        private final Transform mTransform = new Transform() {
            @Override
            public int transform(int position) {
                return outerToChild(position);
            }
        };

        @NonNull
        private final PowerAdapter mAdapter;

        private final int mPosition;

        @Nullable
        private PowerAdapter mObservedAdapter;

        private int mShadowItemCount;

        private int mOuterStart;

        Entry(@NonNull PowerAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        int childToOuter(int childPosition) {
            return getOuterStart() + childPosition;
        }

        int getOuterStart() {
            return mOuterStart;
        }

        int getItemCount() {
            return mShadowItemCount;
        }

        void updateObserver() {
            PowerAdapter adapter = getObserverCount() > 0 ? mAdapter : null;
            if (adapter != mObservedAdapter) {
                if (mObservedAdapter != null) {
                    mObservedAdapter.unregisterDataObserver(mDataObserver);
                    mShadowItemCount = 0;
                }
                mObservedAdapter = adapter;
                if (mObservedAdapter != null) {
                    mObservedAdapter.registerDataObserver(mDataObserver);
                    mShadowItemCount = mObservedAdapter.getItemCount();
                }
            }
        }

        @Override
        public String toString() {
            return "[pos: " + mPosition + ", outerStart: " + mOuterStart + ", count: " + getItemCount()  + "]";
        }
    }

    private static final class OffsetAdapter {

        @NonNull
        private final WeakHashMap<Holder, OffsetHolder> mHolders = new WeakHashMap<>();

        private PowerAdapter mAdapter;
        private Transform mTransform;

        /** The starting position at which this adapter appears in outer coordinate space. */
        private int mOffset;

        @NonNull
        OffsetAdapter set(@NonNull PowerAdapter adapter, @NonNull Transform transform, int offset) {
            mAdapter = adapter;
            mTransform = transform;
            mOffset = offset;
            return this;
        }

        long getItemId(int position) {
            return mAdapter.getItemId(position - mOffset);
        }

        boolean isEnabled(int position) {
            return mAdapter.isEnabled(position - mOffset);
        }

        @NonNull
        View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
            return mAdapter.newView(parent, viewType);
        }

        void bindView(@NonNull View view, @NonNull Holder holder) {
            OffsetHolder offsetHolder = mHolders.get(holder);
            if (offsetHolder == null) {
                offsetHolder = new OffsetHolder(holder);
                mHolders.put(holder, offsetHolder);
            }
            offsetHolder.transform = mTransform;
            offsetHolder.offset = mOffset;
            mAdapter.bindView(view, offsetHolder);
        }

        @NonNull
        ViewType getViewType(int position) {
            return mAdapter.getItemViewType(position - mOffset);
        }

        private static final class OffsetHolder extends HolderWrapper {

            Transform transform;
            int offset;

            OffsetHolder(@NonNull Holder holder) {
                super(holder);
            }

            @Override
            public int getPosition() {
                return transform.transform(super.getPosition());
            }
        }
    }

    private interface Transform {
        int transform(int position);
    }

}
