package com.nextfaze.poweradapters;

import lombok.NonNull;

/**
 * Used to ensure that notifications emitted by the specified {@link PowerAdapter} are consistent with its {@link
 * PowerAdapter#getItemCount()} return value.
 */
final class VerifyingObserver implements DataObserver {

    private int mShadowItemCount;

    @NonNull
    private final PowerAdapter mAdapter;

    VerifyingObserver(@NonNull PowerAdapter adapter) {
        mAdapter = adapter;
        mShadowItemCount = mAdapter.getItemCount();
    }

    @Override
    public void onChanged() {
        mShadowItemCount = mAdapter.getItemCount();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        validate();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        mShadowItemCount += itemCount;
        validate();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        mShadowItemCount -= itemCount;
        validate();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        validate();
    }

    private void validate() {
        int actualItemCount = mAdapter.getItemCount();
        if (mShadowItemCount != actualItemCount) {
            throw new IllegalStateException("Inconsistency detected: expected item count " +
                    mShadowItemCount + " but it is " + actualItemCount);
        }
    }
}
