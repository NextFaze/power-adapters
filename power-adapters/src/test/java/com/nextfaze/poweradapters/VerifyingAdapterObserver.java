package com.nextfaze.poweradapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Used to ensure that notifications emitted by the specified {@link PowerAdapter} are consistent with its {@link
 * PowerAdapter#getItemCount()} return value.
 */
final class VerifyingAdapterObserver implements DataObserver {

    private int mShadowItemCount;

    @NonNull
    private final PowerAdapter mAdapter;

    VerifyingAdapterObserver(@NonNull PowerAdapter adapter) {
        mAdapter = adapter;
        mShadowItemCount = mAdapter.getItemCount();
    }

    @Override
    public void onChanged() {
        mShadowItemCount = mAdapter.getItemCount();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        mShadowItemCount += itemCount;
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        mShadowItemCount -= itemCount;
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        assertItemCountConsistent();
    }

    void assertItemCountConsistent() {
        int actualItemCount = mAdapter.getItemCount();
        if (mShadowItemCount != actualItemCount) {
            throw new IllegalStateException("Inconsistency detected: expected item count " +
                    mShadowItemCount + " but it is " + actualItemCount);
        }
    }
}
