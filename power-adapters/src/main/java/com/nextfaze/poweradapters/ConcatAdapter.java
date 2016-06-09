package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ConcatAdapter extends PowerAdapter {

    @NonNull
    private final Entry[] mEntries;

    @NonNull
    private final Map<ViewType, PowerAdapter> mAdaptersByViewType = new HashMap<>();

    @NonNull
    private final RangeTable.RangeClient mRealRangeClient = new RangeTable.RangeClient() {
        @Override
        public int size() {
            return mEntries.length;
        }

        @Override
        public int getRangeCount(int position) {
            return mEntries[position].mAdapter.getItemCount();
        }

        @Override
        public void setOffset(int position, int offset) {
            mEntries[position].setOffset(offset);
        }
    };

    @NonNull
    private final RangeTable.RangeClient mShadowRangeClient = new RangeTable.RangeClient() {
        @Override
        public int size() {
            return mEntries.length;
        }

        @Override
        public int getRangeCount(int position) {
            return mEntries[position].mShadowItemCount;
        }

        @Override
        public void setOffset(int position, int offset) {
            mEntries[position].setOffset(offset);
        }
    };

    @NonNull
    private final RangeTable mRangeTable = new RangeTable();

    private final boolean mStableIds;

    private int mItemCount;

    ConcatAdapter(@NonNull List<? extends PowerAdapter> adapters) {
        mEntries = new Entry[adapters.size()];
        for (int i = 0; i < mEntries.length; i++) {
            mEntries[i] = new Entry(adapters.get(i));
        }
        // If only a single entry, it's safe to forward it's value directly.
        // Otherwise, must return false because IDs returned by multiple
        // child adapters may collide, falsely indicating equality.
        mStableIds = mEntries.length == 1 && mEntries[0].mAdapter.hasStableIds();
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    @Override
    public boolean hasStableIds() {
        return mStableIds;
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
        PowerAdapter subAdapter = outerToAdapter(position);
        ViewType viewType = subAdapter.getItemViewType(position);
        mAdaptersByViewType.put(viewType, subAdapter);
        return viewType;
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        return mAdaptersByViewType.get(viewType).newView(parent, viewType);
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        outerToAdapter(holder.getPosition()).bindView(view, holder);
    }

    @CallSuper
    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mItemCount = mRangeTable.rebuild(mRealRangeClient);
        if (mItemCount > 0) {
            notifyItemRangeInserted(0, mItemCount);
        }
        for (Entry entry : mEntries) {
            entry.mShadowItemCount = entry.mAdapter.getItemCount();
        }
        for (Entry entry : mEntries) {
            entry.register();
        }
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        for (Entry entry : mEntries) {
            entry.mShadowItemCount = 0;
        }
        for (Entry entry : mEntries) {
            entry.unregister();
        }
        mItemCount = 0;
    }

    @NonNull
    private PowerAdapter outerToAdapter(int outerPosition) {
        Entry entry = mEntries[mRangeTable.findPosition(outerPosition)];
        if (entry.getItemCount() <= 0) {
            throw new AssertionError();
        }
        return entry.mAdapter;
    }

    private final class Entry {

        @NonNull
        private final DataObserver mDataObserver = new DataObserver() {
            @Override
            public void onChanged() {
                mShadowItemCount = mAdapter.getItemCount();
                mItemCount = mRangeTable.rebuild(mShadowRangeClient);
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mShadowItemCount += itemCount;
                mItemCount = mRangeTable.rebuild(mShadowRangeClient);
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mShadowItemCount -= itemCount;
                mItemCount = mRangeTable.rebuild(mShadowRangeClient);
                notifyItemRangeRemoved(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                notifyItemRangeMoved(fromPosition, toPosition, itemCount);
            }
        };

        @NonNull
        private final SubAdapter mAdapter;

        private boolean mObserving;

        private int mShadowItemCount;

        Entry(@NonNull PowerAdapter adapter) {
            mAdapter = new SubAdapter(adapter);
        }

        void setOffset(int offset) {
            mAdapter.setOffset(offset);
        }

        int getOffset() {
            return mAdapter.getOffset();
        }

        int getItemCount() {
            return mShadowItemCount;
        }

        void register() {
            if (!mObserving) {
                mAdapter.registerDataObserver(mDataObserver);
                mObserving = true;
            }
        }

        void unregister() {
            if (mObserving) {
                mAdapter.unregisterDataObserver(mDataObserver);
                mObserving = false;
            }
        }

        @Override
        public String toString() {
            return "[offset: " + getOffset() + ", count: " + getItemCount()  + "]";
        }
    }
}
