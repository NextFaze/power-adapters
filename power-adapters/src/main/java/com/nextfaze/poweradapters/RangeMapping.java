package com.nextfaze.poweradapters;

import android.util.SparseIntArray;
import lombok.NonNull;

final class RangeMapping {

    @NonNull
    private final SparseIntArray mArray = new SparseIntArray();

    @NonNull
    private final RangeClient mRangeClient;

    RangeMapping(@NonNull RangeClient rangeClient) {
        mRangeClient = rangeClient;
    }

    void rebuild() {
        int offset = 0;
        mArray.clear();
        int size = mRangeClient.size();
        for (int i = 0; i < size; i++) {
            mRangeClient.setOffset(i, offset);
            int itemCount = mRangeClient.getRangeCount(i);
            if (itemCount > 0) {
                mArray.put(offset, i);
            }
            offset += itemCount;
        }
    }

    int findPosition(int outerPosition) {
        int rangePosition = mArray.indexOfKey(outerPosition);
        if (rangePosition >= 0) {
            return mArray.valueAt(rangePosition);
        }
        return mArray.valueAt(-rangePosition - 2);
    }

    interface RangeClient {
        int size();

        int getRangeCount(int position);

        void setOffset(int position, int offset);
    }
}
