package com.nextfaze.poweradapters.data;

import android.support.annotation.NonNull;
import com.nextfaze.poweradapters.DataObserver;

/**
 * Used to ensure that notifications emitted by the specified {@link Data} are consistent with its {@link
 * Data#size()} return value.
 */
final class VerifyingDataObserver implements DataObserver {

    private int mShadowSize;

    @NonNull
    private final Data<?> mAdapter;

    VerifyingDataObserver(@NonNull Data<?> data) {
        mAdapter = data;
        mShadowSize = mAdapter.size();
    }

    @Override
    public void onChanged() {
        mShadowSize = mAdapter.size();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        mShadowSize += itemCount;
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        mShadowSize -= itemCount;
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
    }

    void assertSizeConsistent() {
        int actualSize = mAdapter.size();
        if (mShadowSize != actualSize) {
            throw new IllegalStateException("Inconsistency detected: expected size " +
                    mShadowSize + " but it is " + actualSize);
        }
    }
}
