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
    private final RangeMapping mMapping = new RangeMapping(new RangeMapping.RangeClient() {
        @Override
        public int size() {
            return mEntries.length;
        }

        @Override
        public int getRangeCount(int position) {
            return mEntries[position].getItemCount();
        }

        @Override
        public void setOffset(int position, int offset) {
            mEntries[position].setOffset(offset);
        }
    });

    private final boolean mStableIds;

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
        if (getObserverCount() <= 0) {
            rebuild();
        }
        if (mEntries.length == 0) {
            return 0;
        }
        Entry entry = mEntries[mEntries.length - 1];
        return entry.getOffset() + entry.getItemCount();
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
        updateEntryObservers();
        rebuild();
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        updateEntryObservers();
    }

    private void rebuild() {
        mMapping.rebuild();
    }

    @NonNull
    private PowerAdapter outerToAdapter(int outerPosition) {
        Entry entry = mEntries[mMapping.findPosition(outerPosition)];
        if (entry.getItemCount() <= 0) {
            throw new AssertionError();
        }
        return entry.mAdapter;
    }

    private void updateEntryObservers() {
        for (Entry entry : mEntries) {
            entry.updateObserver();
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
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mShadowItemCount += itemCount;
                rebuild();
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mShadowItemCount -= itemCount;
                rebuild();
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
            if (!mObserving) {
                return mAdapter.getItemCount();
            }
            return mShadowItemCount;
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

        @Override
        public String toString() {
            return "[offset: " + getOffset() + ", count: " + getItemCount()  + "]";
        }
    }
}
