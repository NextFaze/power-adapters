package com.nextfaze.poweradapters.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

final class OffsetData<T> extends DataWrapper<T> {

    @NonNull
    private final Data<? extends T> mData;

    private int mOffset;

    public OffsetData(@NonNull Data<? extends T> data, int offset) {
        super(data);
        mData = data;
        mOffset = max(0, offset);
    }

    public int getOffset() {
        return mOffset;
    }

    public void setOffset(int offset) {
        offset = max(0, offset);
        if (offset != mOffset) {
            int oldSize = size();
            mOffset = offset;
            int newSize = size();
            int deltaSize = newSize - oldSize;
            if (deltaSize < 0) {
                notifyItemRangeRemoved(0, abs(deltaSize));
            } else if (deltaSize > 0) {
                notifyItemRangeInserted(0, deltaSize);
            }
        }
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        if (position < 0) {
            throw new IndexOutOfBoundsException();
        }
        return mData.get(position + mOffset, flags);
    }

    @Override
    public int size() {
        return max(0, super.size() - mOffset);
    }

    @Override
    protected void forwardItemRangeChanged(int innerPositionStart, int innerItemCount, @Nullable Object payload) {
        if (innerPositionStart + innerItemCount > mOffset) {
            notifyItemRangeChanged(max(0, innerPositionStart - mOffset),
                    min(innerItemCount, innerItemCount - mOffset + innerPositionStart), payload);
        }
    }

    @Override
    protected void forwardItemRangeInserted(int innerPositionStart, int innerItemCount) {
        int totalInnerItemCountPreInsert = super.size() - innerItemCount;
        int remainingSpace = max(0, mOffset - totalInnerItemCountPreInsert);
        int insertCount = innerItemCount - remainingSpace;
        if (insertCount > 0) {
            notifyItemRangeInserted(max(0, innerPositionStart - mOffset), insertCount);
        }
    }

    @Override
    protected void forwardItemRangeRemoved(int innerPositionStart, int innerItemCount) {
        int removeCount = min(innerItemCount, super.size() + innerItemCount - mOffset);
        if (removeCount > 0) {
            notifyItemRangeRemoved(max(0, innerPositionStart - mOffset), removeCount);
        }
    }

    @Override
    protected void forwardItemRangeMoved(int innerFromPosition, int innerToPosition, int innerItemCount) {
        // TODO: Fine-grained notifications.
        // TODO: Drop notification entirely if out of range.
        notifyDataSetChanged();
    }
}
