package com.nextfaze.powerdata;

import lombok.NonNull;

import static java.lang.Math.max;
import static java.lang.Math.min;

final class LimitData<T> extends DataWrapper<T> {

    @NonNull
    private final Data<? extends T> mData;

    private final int mLimit;

    LimitData(@NonNull Data<? extends T> data, int limit) {
        super(data);
        mData = data;
        mLimit = limit;
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        if (position >= size()) {
            throw new IndexOutOfBoundsException("Position: " + position + ", size: " + size());
        }
        return mData.get(position, flags);
    }

    @Override
    public int size() {
        return min(super.size(), mLimit);
    }

    @Override
    protected void forwardItemRangeChanged(int innerPositionStart, int innerItemCount) {
        if (innerPositionStart < mLimit) {
            notifyItemRangeChanged(innerPositionStart, min(innerItemCount, mLimit - innerPositionStart));
        }
    }

    @Override
    protected void forwardItemRangeInserted(int innerPositionStart, int innerItemCount) {
        if (innerPositionStart < mLimit) {
            notifyItemRangeInserted(innerPositionStart, min(innerItemCount, mLimit - innerPositionStart));
        }
    }

    @Override
    protected void forwardItemRangeRemoved(int innerPositionStart, int innerItemCount) {
        if (innerPositionStart < mLimit) {
            notifyItemRangeRemoved(innerPositionStart, min(innerItemCount, mLimit - innerPositionStart));
        }
    }

    @Override
    protected void forwardItemRangeMoved(int innerFromPosition, int innerToPosition, int innerItemCount) {
        int upperBound = max(innerFromPosition + innerItemCount, innerToPosition + innerItemCount);
        if (upperBound < mLimit) {
            notifyItemRangeMoved(innerFromPosition, innerToPosition, innerItemCount);
        } else {
            // TODO: Split into a removal and an insertion?
            notifyDataChanged();
        }
    }
}
