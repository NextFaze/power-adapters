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
    private final DataObserver mRootDataObserver = new SimpleDataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(rootToOuter(positionStart), itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(rootToOuter(positionStart), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(rootToOuter(positionStart), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyItemRangeMoved(rootToOuter(fromPosition), rootToOuter(toPosition), itemCount);
        }
    };

    @NonNull
    private final Transform mRootTransform = new Transform() {
        @Override
        public int transform(int position) {
            return outerToRoot(position);
        }
    };

    @NonNull
    private final PowerAdapter mRootAdapter;

    /** Reused to wrap an adapter and automatically offset all position calls. Not thread-safe obviously. */
    @NonNull
    private final OffsetAdapter mOffsetAdapter = new OffsetAdapter();

    @NonNull
    private final SparseArray<Entry> mEntries = new SparseArray<>();

    // TODO: Remove mappings from the following when an Entry is closed.
    @NonNull
    private final Map<ViewType, PowerAdapter> mAdaptersByViewType = new HashMap<>();

    public TreeAdapter(@NonNull PowerAdapter rootAdapter) {
        mRootAdapter = rootAdapter;
    }

    public void setExpanded(int position, boolean expanded) {
        Entry entry = mEntries.get(position);
        if (expanded) {
            if (entry == null) {
                entry = new Entry(getChildAdapter(position));
                mEntries.put(position, entry);
                notifyItemRangeInserted(rootToOuter(position) + 1, entry.getItemCount());
            }
        } else {
            if (entry != null) {
                int itemCount = entry.getItemCount();
                entry.dispose();
                mEntries.remove(position);
                notifyItemRangeRemoved(rootToOuter(position) + 1, itemCount);
            }
        }
    }

    public boolean toggleExpanded(int position) {
        boolean expanded = isExpanded(position);
        setExpanded(position, !expanded);
        return !expanded;
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
            itemCount += mEntries.valueAt(i).getItemCount();
        }
        return itemCount;
    }

    @Override
    public long getItemId(int position) {
        return find(position).getItemId(position);
    }

    @Override
    public boolean isEnabled(int position) {
        return find(position).isEnabled(position);
    }

    @NonNull
    @Override
    public ViewType getItemViewType(int position) {
        OffsetAdapter offsetAdapter = find(position);
        ViewType viewType = offsetAdapter.getViewType(position);
        mAdaptersByViewType.put(viewType, offsetAdapter.mAdapter);
        return viewType;
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        return lookup(viewType).newView(parent, viewType);
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        find(holder.getPosition()).bindView(view, holder);
    }

    @CallSuper
    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mRootAdapter.registerDataObserver(mRootDataObserver);
        for (int i = 0; i < mEntries.size(); i++) {
            mEntries.valueAt(i).registerObserversIfNecessary();
        }
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mRootAdapter.unregisterDataObserver(mRootDataObserver);
        for (int i = 0; i < mEntries.size(); i++) {
            mEntries.valueAt(i).unregisterObserversIfNecessary();
        }
    }

    /** Find the adapter corresponding to the position. */
    @NonNull
    private OffsetAdapter find(int position) {
        assertWithinBounds(position);
        int offset = 0;
        int rootOffset = 0;
        int rootItemCount = mRootAdapter.getItemCount();
        for (int i = 0; i < rootItemCount; i++) {
            if (offset >= position) {
                return mOffsetAdapter.set(mRootAdapter, mRootTransform, rootOffset);
            }
            offset++;
            Entry entry = mEntries.get(i);
            if (entry != null) {
                int itemCount = entry.getItemCount();
                if (position - offset < itemCount) {
                    return mOffsetAdapter.set(entry.mAdapter, entry.mTransform, offset);
                }
                offset += itemCount;
                rootOffset += itemCount;
            }
        }
        throw new AssertionError();
    }

    /** Look up the adapter (root or child), based on view type. */
    @NonNull
    private PowerAdapter lookup(@NonNull ViewType viewType) {
        PowerAdapter adapter = mAdaptersByViewType.get(viewType);
        return adapter != null ? adapter : mRootAdapter;
    }

    private int rootToOuter(int rootPosition) {
        int offset = 0;
        for (int i = 0; i < mEntries.size(); i++) {
            Entry entry = mEntries.valueAt(i);
            int entryRootPosition = mEntries.keyAt(i);
            if (entryRootPosition >= rootPosition) {
                break;
            }
            offset += entry.getItemCount();
        }
        return offset + rootPosition;
    }

    private int outerToRoot(int outerPosition) {
        // TODO
        return outerPosition;
    }

    private void assertWithinBounds(int position) {
        int totalItemCount = getItemCount();
        if (position >= totalItemCount) {
            throw new ArrayIndexOutOfBoundsException(format("Index: %d, total size: %d", position, totalItemCount));
        }
    }

    private final class Entry {

        @NonNull
        private final DataObserver mDataObserver = new SimpleDataObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                notifyItemRangeChanged(entryToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                notifyItemRangeInserted(entryToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                notifyItemRangeRemoved(entryToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                notifyItemRangeMoved(entryToOuter(fromPosition), entryToOuter(toPosition), itemCount);
            }
        };

        @NonNull
        private final Transform mTransform = new Transform() {
            @Override
            public int transform(int position) {
                return outerToEntry(position);
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

        private int entryToOuter(int entryPosition) {
            int offset = 0;
            for (int i = 0; i < mEntries.size(); i++) {
                Entry entry = mEntries.valueAt(i);
                if (entry == this) {
                    offset += mEntries.keyAt(i) + 1;
                    break;
                }
                offset += entry.getItemCount();
            }
            return offset + entryPosition;
        }

        private int outerToEntry(int outerPosition) {
            // TODO
            return outerPosition;
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
        private final WeakHashMap<Holder, OffsetHolder> mHolders = new WeakHashMap<>();

        private PowerAdapter mAdapter;
        private Transform mTransform;
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
            offsetHolder.mTransform = mTransform;
            offsetHolder.offset = mOffset;
            mAdapter.bindView(view, offsetHolder);
        }

        @NonNull
        ViewType getViewType(int position) {
            return mAdapter.getItemViewType(position - mOffset);
        }

        private static final class OffsetHolder extends HolderWrapper {

            Transform mTransform;
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

    interface Transform {
        int transform(int position);
    }
}
