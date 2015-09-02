package com.nextfaze.poweradapters;

import lombok.NonNull;

import java.util.Collection;

final class HeaderFooterHelperAdapter extends PowerAdapterWrapper {

    @NonNull
    private final DataObserver mDataObserver = new DataObserver() {
        @Override
        public void onChanged() {
            updateVisibility();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            updateVisibility();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            updateVisibility();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            // No change in size, ignore.
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            // No change in size, ignore.
        }
    };

    /** Items to be presented by this adapter. */
    @NonNull
    private final ItemAdapter mItemAdapter;

    /** The adapter whose contents we are observing. */
    @NonNull
    private final PowerAdapter mAdapter;

    /** Policy used to evaluate whether to show the items. */
    @NonNull
    private final Policy mPolicy;

    HeaderFooterHelperAdapter(@NonNull Collection<Item> items,
                              @NonNull Policy policy,
                              @NonNull PowerAdapter adapter) {
        super(new ItemAdapter(items));
        mAdapter = adapter;
        mItemAdapter = (ItemAdapter) getAdapter();
        mPolicy = policy;
        updateVisibility();
    }

    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mAdapter.registerDataObserver(mDataObserver);
    }

    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mAdapter.unregisterDataObserver(mDataObserver);
    }

    private void updateVisibility() {
        mItemAdapter.setAllVisible(mPolicy.shouldShow());
    }

    interface Policy {
        boolean shouldShow();
    }
}
