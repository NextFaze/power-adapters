package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static java.lang.String.format;
import static java.util.Locale.US;

final class ConcatAdapter extends PowerAdapter {

    /** Reused to wrap an adapter and automatically offset all position calls. */
    @NonNull
    private final OffsetAdapter mOffsetAdapter = new OffsetAdapter();

    @NonNull
    private final SparseArray<Entry> mEntries = new SparseArray<>();

    @NonNull
    private final GroupPool mGroupPool = new GroupPool();

    @NonNull
    private final Map<ViewType, PowerAdapter> mAdaptersByViewType = new HashMap<>();

    /** Contains the mapping of outer positions to groups. */
    @NonNull
    private final SparseArray<Group> mGroups = new SparseArray<>();

    private boolean mDirty = true;

    ConcatAdapter(@NonNull Iterable<? extends PowerAdapter> adapters) {
        int i = 0;
        for (PowerAdapter adapter : adapters) {
            mEntries.append(i, new Entry(adapter));
            i++;
        }
    }

    private void invalidateGroups() {
        mDirty = true;
    }

    private void rebuildGroupsIfNecessary() {
        if (mDirty) {
            for (int i = 0; i < mGroups.size(); i++) {
                mGroupPool.release(mGroups.valueAt(i));
            }
            mGroups.clear();
            int outerStart = 0;
            int i = 0;
            int childAdapterCount = mEntries.size();
            while (i < childAdapterCount) {
                Group group = mGroupPool.obtain();
                Entry entry = mEntries.get(i);
                group.set(i, outerStart, entry);
                mGroups.put(outerStart, group);
                int entryItemCount = entry.getChildItemCount();
                entry.mGroup = group;
                outerStart += entryItemCount;
                i++;
            }
            mDirty = false;
        }
    }

    @NonNull
    private Group groupForPosition(int outerPosition) {
        rebuildGroupsIfNecessary();
        int totalItemCount = getItemCount();
        if (outerPosition >= totalItemCount) {
            throw new ArrayIndexOutOfBoundsException(format(US, "Index: %d, total size: %d", outerPosition, totalItemCount));
        }
        int groupPosition = mGroups.indexOfKey(outerPosition);
        Group group;
        if (groupPosition >= 0) {
            group = mGroups.valueAt(groupPosition);
        } else {
            group = mGroups.valueAt(-groupPosition - 2);
        }
        return group;
    }

    @NonNull
    private PowerAdapter adapterForViewType(@NonNull ViewType viewType) {
        return mAdaptersByViewType.get(viewType);
    }

    @NonNull
    private OffsetAdapter outerToAdapter(int outerPosition) {
        return groupForPosition(outerPosition).adapter();
    }

    private int outerToChild(int outerPosition) {
        return groupForPosition(outerPosition).outerToChild(outerPosition);
    }

    @Override
    public int getItemCount() {
        rebuildGroupsIfNecessary();
        if (mGroups.size() == 0) {
            return 0;
        }
        Group group = mGroups.valueAt(mGroups.size() - 1);
        return group.getOuterStart() + group.size();
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
        // Rebuild index immediately, because later we might rely on it being up-to-date
        // (such as a removal notification from a child).
        invalidateGroups();
        rebuildGroupsIfNecessary();
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
                invalidateGroups();
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                notifyItemRangeChanged(childToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                invalidateGroups();
                notifyItemRangeInserted(childToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                // Must obtain translated position BEFORE invalidating, because a removal notification indicates
                // items that WERE present have been removed.
                int outerPositionStart = childToOuter(positionStart);
                invalidateGroups();
                notifyItemRangeRemoved(outerPositionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                invalidateGroups();
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

        @Nullable
        private PowerAdapter mObservedAdapter;

        @NonNull
        private Group mGroup;

        Entry(@NonNull PowerAdapter adapter) {
            mAdapter = adapter;
        }

        int childToOuter(int childPosition) {
            rebuildGroupsIfNecessary();
            return mGroup.childToOuter(childPosition);
        }

        int getChildItemCount() {
            return mAdapter.getItemCount();
        }

        void updateObserver() {
            PowerAdapter adapter = getObserverCount() > 0 ? mAdapter : null;
            if (adapter != mObservedAdapter) {
                if (mObservedAdapter != null) {
                    mObservedAdapter.unregisterDataObserver(mDataObserver);
                }
                mObservedAdapter = adapter;
                if (mObservedAdapter != null) {
                    mObservedAdapter.registerDataObserver(mDataObserver);
                }
            }
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

    private final class GroupPool {

        @NonNull
        private final ArrayList<Group> mGroups = new ArrayList<>();

        @NonNull
        Group obtain() {
            if (mGroups.isEmpty()) {
                return new Group();
            }
            return mGroups.remove(mGroups.size() - 1);
        }

        void release(@NonNull Group group) {
            mGroups.add(group);
        }
    }

    private final class Group {

        private int mPosition;
        private int mOuterStart;

        @NonNull
        private Entry mEntry;

        @NonNull
        Group set(int position, int outerStart, @NonNull Entry entry) {
            mPosition = position;
            mOuterStart = outerStart;
            mEntry = entry;
            return this;
        }

        int childToOuter(int entryPosition) {
            return getChildOuterStart() + entryPosition;
        }

        int outerToChild(int outerPosition) {
            return outerPosition - getChildOuterStart();
        }

        /** Index of this group within the collection. */
        int getPosition() {
            return mPosition;
        }

        /** Offset of this group in outer adapter coordinate space. */
        int getOuterStart() {
            return mOuterStart;
        }

        int getChildOuterStart() {
            return mOuterStart;
        }

        int size() {
            return mEntry.getChildItemCount();
        }

        @NonNull
        OffsetAdapter adapter() {
            // Outer position maps to the child adapter.
            return mOffsetAdapter.set(mEntry.mAdapter, mEntry.mTransform, getChildOuterStart());
        }

        @Override
        public String toString() {
            return format("%s (%s)", mPosition, size());
        }
    }
}
