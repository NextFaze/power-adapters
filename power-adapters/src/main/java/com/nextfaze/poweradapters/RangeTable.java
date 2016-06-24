package com.nextfaze.poweradapters;

import android.util.SparseIntArray;
import lombok.NonNull;

final class RangeTable {

    @NonNull
    private final SparseIntArray mArray = new SparseIntArray();

    RangeTable() {
    }

    int rebuild(@NonNull RangeClient rangeClient) {
        int offset = 0;
        mArray.clear();
        int size = rangeClient.size();
        for (int i = 0; i < size; i++) {
            rangeClient.setOffset(i, offset);
            int itemCount = rangeClient.getRangeCount(i);
            if (itemCount > 0) {
                mArray.put(offset, i);
            }
            offset += itemCount;
        }
        return offset;
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
