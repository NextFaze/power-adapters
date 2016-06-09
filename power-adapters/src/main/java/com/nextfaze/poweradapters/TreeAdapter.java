package com.nextfaze.poweradapters;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.WeakHashMap;

import static java.util.Collections.swap;

/** Allows hierarchical adapter usage. Note that behaviour of this class is undefined if no observers are registered. */
public final class TreeAdapter extends PowerAdapter {

    @NonNull
    private final DataObserver mRootDataObserver = new SimpleDataObserver() {
        @Override
        public void onChanged() {
            rebuildAllEntriesAndRangeTable();
            updateEntryAdapters();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            for (int i = positionStart; i < positionStart + itemCount; i++) {
                notifyItemChanged(rootToOuter(i));
                Entry entry = mEntries.get(i);
                entry.setAdapter(entry.getAdapter() != null ? getChildAdapter(i) : null);
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            for (int i = positionStart; i < positionStart + itemCount; i++) {
                addEntry(i);
            }
            rebuildRangeTable();
            notifyItemRangeInserted(rootToOuter(positionStart), itemCount);
            for (int i = positionStart; i < positionStart + itemCount; i++) {
                mEntries.get(i).setAdapter(shouldExpand(i) ? getChildAdapter(i) : null);
            }
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            int removeCount = 0;
            int removeStart = rootToOuter(positionStart);
            for (int i = 0; i < itemCount; i++) {
                removeCount += removeEntry(positionStart);
            }
            rebuildRangeTable();
            notifyItemRangeRemoved(removeStart, removeCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            int moveCount = 0;
            for (int i = 0; i < itemCount; i++) {
                moveCount += mEntries.get(fromPosition + i).getItemCount();
            }
            int outerFromPosition = rootToOuter(fromPosition);
            int outerToPosition = rootToOuter(toPosition);
            moveEntries(fromPosition, toPosition, itemCount);
            rebuildRangeTable();
            notifyItemRangeMoved(outerFromPosition, outerToPosition, moveCount);
        }
    };

    @NonNull
    private final ChildAdapterSupplier mChildAdapterSupplier;

    @NonNull
    private final PowerAdapter mRootAdapter;

    @NonNull
    private final SubAdapter mRootSubAdapter;

    @NonNull
    private final ArrayList<Entry> mEntries = new ArrayList<>();

    @NonNull
    private final WeakHashMap<ViewType, PowerAdapter> mAdaptersByViewType = new WeakHashMap<>();

    @NonNull
    private final RangeTable.RangeClient mShadowRangeClient = new RangeTable.RangeClient() {
        @Override
        public int size() {
            return mEntries.size();
        }

        @Override
        public int getRangeCount(int position) {
            return mEntries.get(position).getItemCount();
        }

        @Override
        public void setOffset(int position, int offset) {
            mEntries.get(position).setOffset(offset);
        }
    };

    @NonNull
    private final RangeTable mRangeTable = new RangeTable();

    @NonNull
    private TreeState mState = new TreeState();

    private boolean mAutoExpand;

    public TreeAdapter(@NonNull PowerAdapter rootAdapter, @NonNull ChildAdapterSupplier childAdapterSupplier) {
        mRootAdapter = rootAdapter;
        mRootSubAdapter = new SubAdapter(rootAdapter, new SubAdapter.HolderTransform() {
            @Override
            public int apply(int outerPosition) {
                return outerToRoot(outerPosition);
            }
        });
        mChildAdapterSupplier = childAdapterSupplier;
    }

    /** Returns the parcelable state of the adapter. */
    @NonNull
    public Parcelable saveInstanceState() {
        return mState;
    }

    /**
     * Restores the state of the adapter from a previous state parcelable. Only effective when root adapter {@link
     * PowerAdapter#hasStableIds()}
     */
    public void restoreInstanceState(@Nullable Parcelable parcelable) {
        mState = parcelable instanceof TreeState ? (TreeState) parcelable : new TreeState();
        rebuildAllEntriesAndRangeTable();
        updateEntryAdapters();
    }

    public boolean isAutoExpand() {
        return mAutoExpand;
    }

    public void setAutoExpand(boolean autoExpand) {
        mAutoExpand = autoExpand;
    }

    public boolean isExpanded(int position) {
        if (mRootAdapter.hasStableIds()) {
            return mState.isExpanded(mRootAdapter.getItemId(position));
        }
        Entry entry = mEntries.get(position);
        return entry != null && entry.getAdapter() != null;
    }

    public void setExpanded(int position, boolean expanded) {
        if (mRootAdapter.hasStableIds()) {
            long itemId = mRootAdapter.getItemId(position);
            mState.setExpanded(itemId, expanded);
        }
        if (position < mEntries.size()) {
            Entry entry = mEntries.get(position);
            boolean alreadyExpanded = entry.getAdapter() != null;
            if (expanded != alreadyExpanded) {
                entry.setAdapter(expanded ? getChildAdapter(position) : null);
            }
        }
    }

    public boolean toggleExpanded(int position) {
        boolean expanded = isExpanded(position);
        setExpanded(position, !expanded);
        return !expanded;
    }

    public void setAllExpanded(boolean expanded) {
        for (int i = 0; i < mRootAdapter.getItemCount(); i++) {
            setExpanded(i, expanded);
        }
    }

    @NonNull
    private PowerAdapter getChildAdapter(int position) {
        return mChildAdapterSupplier.get(position);
    }

    @Override
    public int getItemCount() {
        if (getObserverCount() <= 0) {
            return 0;
        }
        if (mEntries.size() == 0) {
            return 0;
        }
        Entry entry = mEntries.get(mEntries.size() - 1);
        return entry.getOffset() + entry.getItemCount();
    }

    /** We don't know all our adapters ahead of time, so can't assume they're stable. */
    @Override
    public boolean hasStableIds() {
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
        PowerAdapter adapter = outerToAdapter(position);
        ViewType viewType = adapter.getItemViewType(position);
        mAdaptersByViewType.put(viewType, adapter);
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
        int insertCount = mRootAdapter.getItemCount();
        mRootAdapter.registerDataObserver(mRootDataObserver);
        rebuildAllEntriesAndRangeTable();
        if (insertCount > 0) {
            notifyItemRangeInserted(0, insertCount);
        }
        updateEntryAdapters();
        updateEntryObservers();
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mRootAdapter.unregisterDataObserver(mRootDataObserver);
        updateEntryObservers();
    }

    private void addEntry(int position) {
        Entry entry = new Entry();
        mEntries.add(position, entry);
    }

    private int removeEntry(int position) {
        Entry entry = mEntries.remove(position);
        // Includes root item.
        int itemCount = entry.getItemCount();
        entry.dispose();
        return itemCount;
    }

    private void moveEntries(int fromPosition, int toPosition, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count <= 0");
        }
        if (fromPosition < toPosition) {
            for (int j = count - 1; j >= 0; j--) {
                for (int i = fromPosition + j; i < toPosition + j; i++) {
                    swap(mEntries, i, i + 1);
                }
            }
        } else {
            for (int j = count - 1; j >= 0; j--) {
                for (int i = fromPosition + j; i > toPosition + j; i--) {
                    swap(mEntries, i, i - 1);
                }
            }
        }
    }

    private void rebuildAllEntriesAndRangeTable() {
        mEntries.clear();
        for (int i = 0; i < mRootAdapter.getItemCount(); i++) {
            mEntries.add(new Entry());
        }
        rebuildRangeTable();
    }

    private void rebuildRangeTable() {
        mRangeTable.rebuild(mShadowRangeClient);
    }

    private void updateEntryAdapters() {
        for (int i = 0; i < mEntries.size(); i++) {
            mEntries.get(i).setAdapter(shouldExpand(i) ? getChildAdapter(i) : null);
        }
    }

    private void updateEntryObservers() {
        for (int i = 0; i < mEntries.size(); i++) {
            mEntries.get(i).updateObserver();
        }
    }

    private boolean shouldExpand(int rootPosition) {
        // Expand if either:
        // - Auto expand is enabled
        // - The saved state indicates this root position is expanded, and the root adapter has stable ids
        return mAutoExpand || mRootAdapter.hasStableIds() &&
                rootPosition < mRootAdapter.getItemCount() &&
                mState.isExpanded(mRootAdapter.getItemId(rootPosition));
    }

    @NonNull
    private PowerAdapter outerToAdapter(int outerPosition) {
        int entryIndex = mRangeTable.findPosition(outerPosition);
        Entry entry = mEntries.get(entryIndex);
        if (entry.getItemCount() <= 0) {
            throw new AssertionError();
        }
        if (outerPosition - entry.getOffset() == 0) {
            mRootSubAdapter.setOffset(entry.getOffset() - entryIndex);
            return mRootSubAdapter;
        }
        return entry.mAdapter;
    }

    @NonNull
    private PowerAdapter adapterForViewType(@NonNull ViewType viewType) {
        PowerAdapter adapter = mAdaptersByViewType.get(viewType);
        return adapter != null ? adapter : mRootAdapter;
    }

    private int outerToRoot(int outerPosition) {
        int entryIndex = mRangeTable.findPosition(outerPosition);
        Entry entry = mEntries.get(entryIndex);
        return outerPosition - (entry.getOffset() - entryIndex);
    }

    private int rootToOuter(int rootPosition) {
        return mEntries.get(rootPosition).getOffset();
    }

    private final class Entry {

        @NonNull
        private final DataObserver mDataObserver = new DataObserver() {
            @Override
            public void onChanged() {
                mShadowItemCount = mAdapter.getItemCount();
                rebuildRangeTable();
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mShadowItemCount += itemCount;
                rebuildRangeTable();
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mShadowItemCount -= itemCount;
                rebuildRangeTable();
                notifyItemRangeRemoved(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                notifyItemRangeMoved(fromPosition, toPosition, itemCount);
            }
        };

        @NonNull
        private final DelegateAdapter mDelegateAdapter;

        @NonNull
        private final SubAdapter mAdapter;

        private boolean mObserving;

        private int mShadowItemCount;

        private int mOffset;

        Entry() {
            mDelegateAdapter = new DelegateAdapter();
            mAdapter = new SubAdapter(mDelegateAdapter);
            updateObserver();
        }

        void setAdapter(@Nullable PowerAdapter adapter) {
            mDelegateAdapter.setDelegate(adapter);
        }

        @Nullable
        PowerAdapter getAdapter() {
            return mDelegateAdapter.getDelegate();
        }

        int getOffset() {
            return mOffset;
        }

        void setOffset(int offset) {
            mOffset = offset;
            mAdapter.setOffset(offset + 1);
        }

        int getItemCount() {
            if (!mObserving) {
                return 0;
            }
            return mShadowItemCount + 1;
        }

        void updateObserver() {
            boolean observe = getObserverCount() > 0;
            if (observe != mObserving) {
                if (mObserving) {
                    mAdapter.unregisterDataObserver(mDataObserver);
                    mShadowItemCount = 0;
                }
                mObserving = observe;
                if (mObserving) {
                    mShadowItemCount = mAdapter.getItemCount();
                    mAdapter.registerDataObserver(mDataObserver);
                }
            }
        }

        void dispose() {
            if (mObserving) {
                mAdapter.unregisterDataObserver(mDataObserver);
                mShadowItemCount = 0;
                mObserving = false;
            }
        }

        @Override
        public String toString() {
            return "[offset: " + getOffset() + ", count: " + getItemCount()  + "]";
        }
    }

    static final class TreeState implements Parcelable {

        public static final Creator<TreeState> CREATOR = new Creator<TreeState>() {

            @NonNull
            public TreeState createFromParcel(@NonNull Parcel parcel) {
                return new TreeState(parcel);
            }

            @NonNull
            public TreeState[] newArray(int size) {
                return new TreeState[size];
            }
        };

        @NonNull
        private final HashSet<Long> mExpanded;

        TreeState(@NonNull Parcel parcel) {
            //noinspection unchecked
            mExpanded = (HashSet<Long>) parcel.readSerializable();
        }

        TreeState() {
            mExpanded = new HashSet<>();
        }

        void setExpanded(long itemId, boolean expanded) {
            if (itemId != NO_ID) {
                if (expanded) {
                    mExpanded.add(itemId);
                } else {
                    mExpanded.remove(itemId);
                }
            }
        }

        boolean isExpanded(long itemId) {
            //noinspection SimplifiableIfStatement
            if (itemId == NO_ID) {
                return false;
            }
            return mExpanded.contains(itemId);
        }

        void clear() {
            mExpanded.clear();
        }

        boolean isEmpty() {
            return mExpanded.isEmpty();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel parcel, int flags) {
            parcel.writeSerializable(mExpanded);
        }
    }

    public interface ChildAdapterSupplier {
        @NonNull
        PowerAdapter get(int position);
    }
}
