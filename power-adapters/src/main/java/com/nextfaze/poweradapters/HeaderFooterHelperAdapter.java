package com.nextfaze.poweradapters;

import lombok.NonNull;

import java.util.Collection;

final class HeaderFooterHelperAdapter extends ItemAdapter {

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

    /** The adapter whose contents we are observing. */
    @NonNull
    private final PowerAdapter mAdapter;

    /** Policy used to evaluate whether to show the items. */
    @NonNull
    private final VisibilityPolicy mVisibilityPolicy;

    HeaderFooterHelperAdapter(@NonNull Collection<Item> items,
                              @NonNull VisibilityPolicy visibilityPolicy,
                              @NonNull PowerAdapter adapter) {
        super(items);
        mAdapter = adapter;
        mVisibilityPolicy = visibilityPolicy;
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
        setAllVisible(mVisibilityPolicy.shouldShow());
    }

    interface VisibilityPolicy {
        boolean shouldShow();
    }
}
