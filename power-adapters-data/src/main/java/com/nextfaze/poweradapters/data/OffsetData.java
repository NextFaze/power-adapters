package com.nextfaze.poweradapters.data;

import lombok.NonNull;

import static java.lang.Math.max;
import static java.lang.Math.min;

final class OffsetData<T> extends DataWrapper<T> {

    @NonNull
    private final Data<? extends T> mData;

    private final int mOffset;

    public OffsetData(@NonNull Data<? extends T> data, int offset) {
        super(data);
        mData = data;
        mOffset = max(0, offset);
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        return mData.get(position + mOffset, flags);
    }

    @Override
    public int size() {
        return super.size() - mOffset;
    }

    @Override
    protected void forwardItemRangeChanged(int innerPositionStart, int innerItemCount) {
        if (innerPositionStart + innerItemCount > mOffset) {
            notifyItemRangeChanged(max(0, innerPositionStart - mOffset),
                    min(innerItemCount, innerItemCount - mOffset + innerPositionStart));
        }
    }

    @Override
    protected void forwardItemRangeInserted(int innerPositionStart, int innerItemCount) {
        if (innerPositionStart + innerItemCount > mOffset) {
            notifyItemRangeInserted(max(0, innerPositionStart - mOffset),
                    min(innerItemCount, innerItemCount - mOffset + innerPositionStart));
        }
    }

    @Override
    protected void forwardItemRangeRemoved(int innerPositionStart, int innerItemCount) {
        if (innerPositionStart + innerItemCount > mOffset) {
            notifyItemRangeRemoved(max(0, innerPositionStart - mOffset),
                    min(innerItemCount, innerItemCount - mOffset + innerPositionStart));
        }
    }

    @Override
    protected void forwardItemRangeMoved(int innerFromPosition, int innerToPosition, int innerItemCount) {
        // TODO: Fine-grained notifications.
        notifyDataChanged();
    }
}
