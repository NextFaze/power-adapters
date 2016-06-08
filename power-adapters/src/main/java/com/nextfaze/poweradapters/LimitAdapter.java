package com.nextfaze.poweradapters;

import lombok.NonNull;

import static java.lang.Math.*;

public final class LimitAdapter extends PowerAdapterWrapper {

    private int mLimit;

    /**
     * Maintain our own item count, because we split notifications, and our {@link #getItemCount()} is expected to
     * remain consistent with those notifications.
     */
    private int mItemCount;

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
        if (getObserverCount() > 0) {
            return mItemCount;
        }
        return getLimitedItemCount();
    }

    private int getLimitedItemCount() {
        return max(0, min(mLimit, super.getItemCount()));
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(assertWithinRange(position));
    }

    @NonNull
    @Override
    public ViewType getItemViewType(int position) {
        return super.getItemViewType(assertWithinRange(position));
    }

    @Override
    public boolean isEnabled(int position) {
        return super.isEnabled(assertWithinRange(position));
    }

    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mItemCount = max(0, min(mLimit, super.getItemCount()));
    }

    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mItemCount = 0;
    }

    @Override
    protected void forwardChanged() {
        super.forwardChanged();
        mItemCount = max(0, min(mLimit, super.getItemCount()));
    }

    @Override
    protected void forwardItemRangeChanged(int innerPositionStart, int innerItemCount) {
        if (innerItemCount > 0 && innerPositionStart < mLimit) {
            notifyItemRangeChanged(innerPositionStart, min(innerItemCount, mLimit - innerPositionStart));
        }
    }

    @Override
    protected void forwardItemRangeInserted(int innerPositionStart, int innerItemCount) {
        if (innerItemCount > 0 && innerPositionStart < mLimit) {
            int innerTotalPostInsert = super.getItemCount();
            int innerTotalPreInsert = innerTotalPostInsert - innerItemCount;
            if (innerTotalPreInsert >= mLimit) {
                notifyItemRangeChanged(innerPositionStart, mLimit - innerPositionStart);
            } else {
                int insertCount = min(mLimit - innerPositionStart, innerItemCount);
                if (innerPositionStart <= innerTotalPreInsert) {
                    int remainingSpace = mLimit - innerTotalPreInsert;
                    int removeCount = insertCount - remainingSpace;
                    if (removeCount > 0) {
                        mItemCount -= removeCount;
                        notifyItemRangeRemoved(innerTotalPreInsert - removeCount, removeCount);
                    }
                }
                mItemCount += insertCount;
                notifyItemRangeInserted(innerPositionStart, insertCount);
            }
        }
    }

    @Override
    protected void forwardItemRangeRemoved(int innerPositionStart, int innerItemCount) {
        if (innerItemCount > 0 && innerPositionStart < mLimit) {
            int innerTotalPostRemove = super.getItemCount();
            int innerTotalPreRemove = innerTotalPostRemove + innerItemCount;
            if (innerTotalPostRemove >= mLimit) {
                notifyItemRangeChanged(innerPositionStart, mLimit - innerPositionStart);
            } else {
                int removeCount = min(mLimit - innerPositionStart, innerItemCount);
                mItemCount -= removeCount;
                notifyItemRangeRemoved(innerPositionStart, removeCount);
                if (innerPositionStart + innerItemCount >= mLimit) {
                    int insertCount = innerTotalPreRemove - innerPositionStart - innerItemCount;
                    if (insertCount > 0) {
                        mItemCount += insertCount;
                        notifyItemRangeInserted(innerPositionStart, insertCount);
                    }
                }
            }
        }
    }

    @Override
    protected void forwardItemRangeMoved(int innerFromPosition, int innerToPosition, int innerItemCount) {
        // TODO: Fine-grained notifications.
        // TODO: Drop notification entirely if out of range.
        notifyDataSetChanged();
    }

    private int assertWithinRange(int position) {
        if (position < 0 || position >= getItemCount()) {
            throw new IndexOutOfBoundsException();
        }
        return position;
    }
}
