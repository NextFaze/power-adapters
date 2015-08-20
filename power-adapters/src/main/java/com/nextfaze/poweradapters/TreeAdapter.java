package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static java.lang.String.format;

public abstract class TreeAdapter extends AbstractPowerAdapter {

    @NonNull
    private final DataObserver mRootDataObserver = new SimpleDataObserver() {
        @Override
        public void onChanged() {
            invalidateGroups();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            invalidateGroups();
            notifyItemRangeChanged(rootToOuter(positionStart), itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            invalidateGroups();
            notifyItemRangeInserted(rootToOuter(positionStart), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            invalidateGroups();
            notifyItemRangeRemoved(rootToOuter(positionStart), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            invalidateGroups();
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

    @NonNull
    private final List<Group> mGroups = new ArrayList<>();

    boolean mDirty = true;

    public TreeAdapter(@NonNull PowerAdapter rootAdapter) {
        mRootAdapter = rootAdapter;
    }

    public void setExpanded(int position, boolean expanded) {
        Entry entry = mEntries.get(position);
        if (expanded) {
            if (entry == null) {
                entry = new Entry(getChildAdapter(position), mGroups.get(position));
                mEntries.put(position, entry);
                invalidateGroups();
                notifyItemRangeInserted(rootToOuter(position) + 1, entry.getItemCount());
            }
        } else {
            if (entry != null) {
                int itemCount = entry.getItemCount();
                entry.dispose();
                mEntries.remove(position);
                invalidateGroups();
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

    static final class Group implements Comparable<Group> {

        private final int mOuterStart; // inclusive
        private final int mOuterEnd; // exclusive
        private final int mRootStart;
        private final int mEntryStart;

        Group(int outerStart, int outerEnd, int rootStart) {
            mOuterStart = outerStart;
            mOuterEnd = outerEnd;
            mRootStart = rootStart;
            mEntryStart = outerStart + 1;
        }

        @NonNull
        static Group position(int position) {
            return new Group(position, position + 1, 0);
        }

        @Override
        public int compareTo(Group o) {
            if (mOuterStart < o.mOuterStart) {
                return -1;
            }
            if (mOuterStart >= o.mOuterEnd) {
                return +1;
            }
            return 0;
        }
    }

    private void invalidateGroups() {
        mDirty = true;
        rebuildGroupsIfNecessary();
    }

    private void rebuildGroupsIfNecessary() {
        if (mDirty) {
            mGroups.clear();
            int outerStart = 0;
            int rootStart = 0;
            for (int i = 0; i < mRootAdapter.getItemCount(); i++) {
                Entry entry = mEntries.get(i);
                if (entry != null) {
                    int entryItemCount = entry.getItemCount();
                    mGroups.add(new Group(outerStart, outerStart + entryItemCount + 1, rootStart));
                    outerStart += entryItemCount + 1;
                    rootStart += entryItemCount;
                } else {
                    mGroups.add(new Group(outerStart, outerStart + 1, rootStart));
                    outerStart++;
                }
            }
            mDirty = false;
        }
    }

    @Override
    public int getItemCount() {
        rebuildGroupsIfNecessary();
        int total = 0;
        for (int i = 0; i < mRootAdapter.getItemCount(); i++) {
            total++;
            Entry entry = mEntries.get(i);
            if (entry != null) {
                total += entry.getItemCount();
            }
        }
        return total;
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

    private int binarySearchForGroupPosition(int outerPosition) {
        return Collections.binarySearch(mGroups, Group.position(outerPosition));
    }

    private int linearSearchForGroupPosition( int outerPosition) {
        for (int i = 0; i < mGroups.size(); i++) {
            Group group = mGroups.get(i);
            if (outerPosition >= group.mOuterStart && outerPosition < group.mOuterEnd) {
                return i;
            }
        }
        return -1;
    }

    private int searchForGroupPosition( int outerPosition) {
        // TODO: Get binary search working.
//        return binarySearchForGroupPosition(outerPosition);
        return linearSearchForGroupPosition(outerPosition);
    }

    @NonNull
    private OffsetAdapter find(int outerPosition) {
        assertWithinBounds(outerPosition);
        int groupPosition = searchForGroupPosition(outerPosition);
        Group group = mGroups.get(groupPosition);
        if (outerPosition - group.mOuterStart == 0) {
            return mOffsetAdapter.set(mRootAdapter, mRootTransform, group.mRootStart);
        }
        Entry entry = mEntries.get(groupPosition);
        return mOffsetAdapter.set(entry.mAdapter, entry.mTransform, group.mEntryStart);
    }

    /** Look up the adapter (root or child), based on view type. */
    @NonNull
    private PowerAdapter lookup(@NonNull ViewType viewType) {
        PowerAdapter adapter = mAdaptersByViewType.get(viewType);
        return adapter != null ? adapter : mRootAdapter;
    }

    private int rootToOuter(int rootPosition) {
        return mGroups.get(rootPosition).mOuterStart;
    }

    private int outerToRoot(int outerPosition) {
        int groupPosition = searchForGroupPosition(outerPosition);
        Group group = mGroups.get(groupPosition);
        return outerPosition - group.mRootStart;
    }

    private void assertWithinBounds(int outerPosition) {
        int totalItemCount = getItemCount();
        if (outerPosition >= totalItemCount) {
            throw new ArrayIndexOutOfBoundsException(format("Index: %d, total size: %d", outerPosition, totalItemCount));
        }
    }

    private final class Entry {

        @NonNull
        private final DataObserver mDataObserver = new SimpleDataObserver() {
            @Override
            public void onChanged() {
                invalidateGroups();
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                invalidateGroups();
                notifyItemRangeChanged(entryToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                invalidateGroups();
                notifyItemRangeInserted(entryToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                invalidateGroups();
                notifyItemRangeRemoved(entryToOuter(positionStart), itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                invalidateGroups();
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

        @NonNull
        private final Group mGroup;

        Entry(@NonNull PowerAdapter adapter, @NonNull Group group) {
            mAdapter = adapter;
            mGroup = group;
            registerObserversIfNecessary();
        }

        private int entryToOuter(int entryPosition) {
            return mGroup.mEntryStart + entryPosition;
        }

        private int outerToEntry(int outerPosition) {
            int groupPosition = searchForGroupPosition(outerPosition);
            Group group = mGroups.get(groupPosition);
            return outerPosition - group.mEntryStart;
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
//                return super.getPosition() - offset;
                return transform.transform(super.getPosition());
            }
        }
    }

    interface Transform {
        int transform(int position);
    }
}
