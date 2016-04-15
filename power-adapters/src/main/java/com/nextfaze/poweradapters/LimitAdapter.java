package com.nextfaze.poweradapters;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import static java.lang.Math.*;

@Accessors(prefix = "m")
final class LimitAdapter extends PowerAdapterWrapper {

    @Getter
    private int mLimit;

    public LimitAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
    }

    public LimitAdapter(@NonNull PowerAdapter adapter, int limit) {
        super(adapter);
        mLimit = max(0, limit);
    }

    public int getLimit() {
        return mLimit;
    }

    public void setLimit(int limit) {
        limit = max(0, limit);
        if (limit != mLimit) {
            int oldSize = getItemCount();
            mLimit = limit;
            int newSize = getItemCount();
            int deltaSize = newSize - oldSize;
            if (deltaSize < 0) {
                notifyItemRangeRemoved(oldSize + deltaSize, abs(deltaSize));
            } else if (deltaSize > 0) {
                notifyItemRangeInserted(oldSize, deltaSize);
            }
        }
    }

    @Override
    public int getItemCount() {
        return max(0, min(mLimit, super.getItemCount()));
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
        // TODO: Fine-grained notifications.
        notifyDataSetChanged();
    }
}
