package com.nextfaze.poweradapters;

import lombok.NonNull;

import static java.lang.Math.*;

final class OffsetAdapter extends PowerAdapterWrapper {

    private int mOffset;

    public OffsetAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
    }

    public OffsetAdapter(@NonNull PowerAdapter adapter, int offset) {
        super(adapter);
        mOffset = max(0, offset);
    }

    public int getOffset() {
        return mOffset;
    }

    public void setOffset(int offset) {
        offset = max(0, offset);
        if (offset != mOffset) {
            int oldSize = getItemCount();
            mOffset = offset;
            int newSize = getItemCount();
            int deltaSize = newSize - oldSize;
            if (deltaSize < 0) {
                notifyItemRangeRemoved(0, abs(deltaSize));
            } else if (deltaSize > 0) {
                notifyItemRangeInserted(0, deltaSize);
            }
        }
    }

    @Override
    public int getItemCount() {
        return max(0, super.getItemCount() - mOffset);
    }

    @Override
    protected int innerToOuter(int innerPosition) {
        return super.innerToOuter(innerPosition) - mOffset;
    }

    @Override
    protected int outerToInner(int outerPosition) {
        return super.outerToInner(outerPosition) + mOffset;
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
        // TODO: Drop notification entirely if out of range.
        notifyDataSetChanged();
    }
}
