package com.nextfaze.poweradapters;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
final class DividerAdapter extends PowerAdapterWrapper {

    @NonNull
    private final ViewType mLeadingViewType = new ViewType();

    @NonNull
    private final ViewType mTrailingViewType = new ViewType();

    @NonNull
    private final ViewType mInnerViewType = new ViewType();

    @NonNull
    private final DividerAdapterBuilder.EmptyPolicy mEmptyPolicy;

    @Nullable
    private final Item mLeadingItem;

    @Nullable
    private final Item mTrailingItem;

    @Nullable
    private final Item mInnerItem;

    DividerAdapter(@NonNull PowerAdapter adapter,
                   @NonNull DividerAdapterBuilder.EmptyPolicy emptyPolicy,
                   @Nullable Item leadingItem,
                   @Nullable Item trailingItem,
                   @Nullable Item innerItem) {
        super(adapter);
        mLeadingItem = leadingItem;
        mTrailingItem = trailingItem;
        mInnerItem = innerItem;
        mEmptyPolicy = emptyPolicy;
    }

    @Override
    public long getItemId(int position) {
        if (isDivider(position)) {
            return NO_ID;
        }
        return super.getItemId(position);
    }

    @Override
    public boolean isEnabled(int position) {
        //noinspection SimplifiableIfStatement
        if (isDivider(position)) {
            return false;
        }
        return super.isEnabled(position);
    }

    @NonNull
    @Override
    public ViewType getItemViewType(int position) {
        if (isInnerDivider(position)) {
            return mInnerViewType;
        }
        if (isLeadingDivider(position)) {
            return mLeadingViewType;
        }
        if (isTrailingDivider(position)) {
            return mTrailingViewType;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        if (viewType == mInnerViewType) {
            if (mInnerItem == null) {
                throw new IllegalStateException();
            }
            return mInnerItem.create(parent);
        }
        if (viewType == mLeadingViewType) {
            if (mLeadingItem == null) {
                throw new IllegalStateException();
            }
            return mLeadingItem.create(parent);
        }
        if (viewType == mTrailingViewType) {
            if (mTrailingItem == null) {
                throw new IllegalStateException();
            }
            return mTrailingItem.create(parent);
        }
        return super.newView(parent, viewType);
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        ViewType viewType = getItemViewType(holder.getPosition());
        if (viewType == mInnerViewType) {
            return;
        }
        if (viewType == mLeadingViewType) {
            return;
        }
        if (viewType == mTrailingViewType) {
            return;
        }
        super.bindView(view, holder);
    }

    @Override
    public int getItemCount() {
        int innerItemCount = super.getItemCount();
        int count;
        if (isInnerVisible(innerItemCount)) {
            count = innerItemCount * 2 - 1;
        } else {
            count = innerItemCount;
        }
        if (isLeadingVisible(innerItemCount)) {
            ++count;
        }
        if (isTrailingVisible(innerItemCount)) {
            ++count;
        }
        return count;
    }

    private boolean isLeadingVisible() {
        return isLeadingVisible(super.getItemCount());
    }

    private boolean isLeadingVisible(int itemCount) {
        return mLeadingItem != null && mEmptyPolicy.shouldShowLeading(itemCount);
    }

    private boolean didLeadingBecomeVisible(int itemCountBefore, int itemCountAfter) {
        return !isLeadingVisible(itemCountBefore) && isLeadingVisible(itemCountAfter);
    }

    private boolean didLeadingBecomeInvisible(int itemCountBefore, int itemCountAfter) {
        return isLeadingVisible(itemCountBefore) && !isLeadingVisible(itemCountAfter);
    }

    private boolean isLeadingStillVisible(int itemCountBefore, int itemCountAfter) {
        return isLeadingVisible(itemCountBefore) && isLeadingVisible(itemCountAfter);
    }

    private boolean didTrailingBecomeVisible(int itemCountBefore, int itemCountAfter) {
        return !isTrailingVisible(itemCountBefore) && isTrailingVisible(itemCountAfter);
    }

    private boolean didTrailingBecomeInvisible(int itemCountBefore, int itemCountAfter) {
        return isTrailingVisible(itemCountBefore) && !isTrailingVisible(itemCountAfter);
    }

    private boolean isTrailingStillVisible(int itemCountBefore, int itemCountAfter) {
        return isTrailingVisible(itemCountBefore) && isTrailingVisible(itemCountAfter);
    }

    private boolean isTrailingVisible() {
        return isTrailingVisible(super.getItemCount());
    }

    private boolean isTrailingVisible(int itemCount) {
        return mTrailingItem != null && mEmptyPolicy.shouldShowTrailing(itemCount);
    }

    private boolean isInnerVisible() {
        return isInnerVisible(super.getItemCount());
    }

    private boolean isInnerVisible(int itemCount) {
        return mInnerItem != null && mEmptyPolicy.shouldShowInner(itemCount);
    }

    @Override
    protected int outerToInner(int outerPosition) {
        return outerToInner(outerPosition, super.getItemCount());
    }

    @Override
    protected int innerToOuter(int innerPosition) {
        return innerToOuter(innerPosition, super.getItemCount());
    }

    private int outerToInner(final int outerPosition, final int itemCount) {
        int innerPosition = outerPosition;
        if (isLeadingVisible(itemCount)) {
            innerPosition--;
        }
        if (isInnerVisible(itemCount)) {
            innerPosition /= 2;
        }
        // TODO: Account for trailing?
        return innerPosition;
    }

    private int innerToOuter(final int innerPosition, final int itemCount) {
        int outerPosition = innerPosition;
        if (isInnerVisible(itemCount)) {
            outerPosition *= 2;
        }
        if (isLeadingVisible(itemCount)) {
            outerPosition++;
        }
        // TODO: Account for trailing?
        return outerPosition;
    }

    private int innerToOuterCount(int innerPositionStart,
                                  int innerItemCount,
                                  int innerTotalItemCountBefore,
                                  int innerTotalItemCountAfter,
                                  boolean insertion) {
        if (insertion) {
            int itemCount = innerItemCount;
            boolean rangeIncludesFirstItem = innerPositionStart == 0;
            boolean rangeIncludesLastItem = innerPositionStart + itemCount >= innerTotalItemCountAfter;
            if (!isLeadingVisible(innerTotalItemCountBefore) && isLeadingVisible(innerTotalItemCountAfter) && rangeIncludesFirstItem) {
                itemCount++;
            }
            if (!isTrailingVisible(innerTotalItemCountAfter) && rangeIncludesLastItem) {
                itemCount++;
            }
            if (isInnerVisible(innerTotalItemCountAfter)) {
                itemCount += innerItemCount;
            }
            if (!isTrailingVisible(innerTotalItemCountAfter) && rangeIncludesLastItem) {
                itemCount--;
            }
            return itemCount;
        } else {
            int itemCount = innerItemCount;
            boolean rangeIncludesFirstItem = innerPositionStart == 0;
            boolean rangeIncludesLastItem = innerPositionStart + itemCount >= innerTotalItemCountBefore;
            if (isLeadingVisible(innerTotalItemCountBefore) && !isLeadingVisible(innerTotalItemCountAfter) && rangeIncludesFirstItem) {
                itemCount++;
            }
            if (!isTrailingVisible(innerTotalItemCountBefore) && rangeIncludesLastItem) {
                itemCount++;
            }
            if (isInnerVisible(innerTotalItemCountBefore)) {
                itemCount += innerItemCount;
            }
            if (!isTrailingVisible(innerTotalItemCountBefore) && rangeIncludesLastItem) {
                itemCount--;
            }
            return itemCount;
        }
    }

    // TODO: Account for when an inner turns into a leading/trailing, and vice versa. If we don't, it will appear as if an additional item is being removed/inserted.

    @Override
    protected void forwardChanged() {
        notifyDataSetChanged();
    }

    @Override
    protected void forwardItemRangeChanged(final int innerPositionStart, final int innerItemCount) {
        int innerTotalItemCount = super.getItemCount();
        for (int i = 0; i < innerItemCount; i++) {
            int outerPositionStart = (innerPositionStart + i) * 2;
            if (isLeadingVisible(innerTotalItemCount)) {
                outerPositionStart++;
            }
            notifyItemRangeChanged(outerPositionStart, 1);
        }
    }

    @Override
    protected void forwardItemRangeInserted(final int innerPositionStart, final int innerItemCount) {
        int innerTotalItemCountBefore = super.getItemCount() - innerItemCount;
        int innerTotalItemCountAfter = super.getItemCount();
        int outerPositionStart = innerPositionStart;
        if (isInnerVisible(innerTotalItemCountAfter)) {
            outerPositionStart *= 2;
        }
        if (!isTrailingVisible(innerTotalItemCountAfter)) {
            outerPositionStart--;
        }
        if (isLeadingStillVisible(innerTotalItemCountBefore, innerTotalItemCountAfter)) {
            outerPositionStart++;
        }
        int outerItemCount = innerItemCount;
        boolean rangeIncludesFirstItem = innerPositionStart == 0;
        boolean rangeIncludesLastItem = innerPositionStart + outerItemCount >= innerTotalItemCountAfter;
        if (!isLeadingVisible(innerTotalItemCountBefore) && isLeadingVisible(innerTotalItemCountAfter) && rangeIncludesFirstItem) {
            outerItemCount++;
        }
        if (!isTrailingVisible(innerTotalItemCountBefore) && isTrailingVisible(innerTotalItemCountAfter) && rangeIncludesLastItem) {
            outerItemCount++;
        }
        if (isInnerVisible(innerTotalItemCountAfter)) {
            outerItemCount += innerItemCount;
        }
        notifyItemRangeInserted(outerPositionStart, outerItemCount);
    }

    @Override
    protected void forwardItemRangeRemoved(final int innerPositionStart, final int innerItemCount) {
        int innerTotalItemCountBefore = super.getItemCount() + innerItemCount;
        int innerTotalItemCountAfter = super.getItemCount();
        int outerPositionStart = innerPositionStart;
        if (isInnerVisible(innerTotalItemCountBefore)) {
            outerPositionStart *= 2;
        }
        if (!isTrailingVisible(innerTotalItemCountBefore)) {
            outerPositionStart--;
        }
        if (isLeadingStillVisible(innerTotalItemCountBefore, innerTotalItemCountAfter)) {
            outerPositionStart++;
        }
        int outerItemCount = innerItemCount;
        boolean rangeIncludesFirstItem = innerPositionStart == 0;
        boolean rangeIncludesLastItem = innerPositionStart + outerItemCount >= innerTotalItemCountBefore;
        if (isLeadingVisible(innerTotalItemCountBefore) && !isLeadingVisible(innerTotalItemCountAfter) &&
                rangeIncludesFirstItem) {
            outerItemCount++;
        }
        if (isTrailingVisible(innerTotalItemCountBefore) && !isTrailingVisible(innerTotalItemCountAfter) && rangeIncludesLastItem) {
            outerItemCount++;
        }
        if (isInnerVisible(innerTotalItemCountBefore)) {
            outerItemCount += innerItemCount;
        }
        notifyItemRangeRemoved(outerPositionStart, outerItemCount);
    }

    @Override
    protected void forwardItemRangeMoved(final int innerFromPosition, final int innerToPosition, final int innerItemCount) {
        // TODO: Implement proper fine-grained notifications for bulk moves.
        notifyDataSetChanged();
    }

    private boolean isDivider(int position) {
        return isInnerDivider(position) || isLeadingDivider(position) || isTrailingDivider(position);
    }

    private boolean isInnerDivider(int position) {
        if (isLeadingDivider(position)) {
            return false;
        }
        if (isTrailingDivider(position)) {
            return false;
        }
        if (!isLeadingVisible()) {
            ++position;
        }
        //noinspection SimplifiableIfStatement
        if (isInnerVisible()) {
            return position % 2 == 0;
        }
        return false;
    }

    private boolean isLeadingDivider(int position) {
        return isLeadingVisible() && position == 0;
    }

    private boolean isTrailingDivider(int position) {
        return isTrailingVisible() && position == getItemCount() - 1;
    }
}
