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
    private final Item mLeadingItemResource;

    @Nullable
    private final Item mTrailingItemResource;

    @Nullable
    private final Item mInnerItemResource;

    DividerAdapter(@NonNull PowerAdapter adapter,
                   @NonNull DividerAdapterBuilder.EmptyPolicy emptyPolicy,
                   @Nullable Item leadingItemResource,
                   @Nullable Item trailingItemResource,
                   @Nullable Item innerItemResource) {
        super(adapter);
        mLeadingItemResource = leadingItemResource;
        mTrailingItemResource = trailingItemResource;
        mInnerItemResource = innerItemResource;
        mEmptyPolicy = emptyPolicy;
    }

    @Override
    public long getItemId(int position) {
        if (isDivider(position)) {
            return NO_ID;
        }
        return super.getItemId(position);
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
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        if (viewType == mInnerViewType) {
            if (mInnerItemResource == null) {
                throw new IllegalStateException();
            }
            return mInnerItemResource.create(parent);
        }
        if (viewType == mLeadingViewType) {
            if (mLeadingItemResource == null) {
                throw new IllegalStateException();
            }
            return mLeadingItemResource.create(parent);
        }
        if (viewType == mTrailingViewType) {
            if (mTrailingItemResource == null) {
                throw new IllegalStateException();
            }
            return mTrailingItemResource.create(parent);
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
        int superCount = super.getItemCount();
        int count;
        if (isInnerVisible()) {
            count = superCount * 2 - 1;
        } else {
            count = superCount;
        }
        if (isLeadingVisible()) {
            ++count;
        }
        if (isTrailingVisible()) {
            ++count;
        }
        return count;
    }

    private boolean isLeadingVisible() {
        return isLeadingVisible(super.getItemCount());
    }

    private boolean isLeadingVisible(int itemCount) {
        return mLeadingItemResource != null && mEmptyPolicy.shouldShowLeading(itemCount);
    }

    private boolean isTrailingVisible() {
        return isTrailingVisible(super.getItemCount());
    }

    private boolean isTrailingVisible(int itemCount) {
        return mTrailingItemResource != null && mEmptyPolicy.shouldShowTrailing(itemCount);
    }

    private boolean isInnerVisible() {
        return isInnerVisible(super.getItemCount());
    }

    private boolean isInnerVisible(int itemCount) {
        return mInnerItemResource != null && mEmptyPolicy.shouldShowInner(itemCount);
    }

    @Override
    protected int outerToInner(int outerPosition) {
        return outerToInner(outerPosition, super.getItemCount());
    }

    private int outerToInner(int outerPosition, int itemCount) {
        if (isLeadingVisible(itemCount)) {
            return (outerPosition - 1) / 2;
        }
        return outerPosition / 2;
    }

    @Override
    protected int innerToOuter(int innerPosition) {
        return innerToOuter(innerPosition, super.getItemCount());
    }

    private int innerToOuter(int innerPosition, int itemCount) {
        if (isLeadingVisible(itemCount)) {
            return (innerPosition * 2) + 1;
        }
        return innerPosition * 2;
    }

    private int innerToOuterCount(int innerPositionStart, int innerItemCount, int innerTotalItemCount) {
        int itemCount = innerItemCount;
        boolean rangeIncludesFirstItem = innerPositionStart == 0;
        boolean rangeIncludesLastItem = innerPositionStart + itemCount >= innerTotalItemCount;
        if (isLeadingVisible(innerTotalItemCount) && rangeIncludesFirstItem && rangeIncludesLastItem) {
            itemCount++;
        }
        if (isInnerVisible(innerTotalItemCount)) {
            itemCount += innerItemCount;
        }
        if (!isTrailingVisible(innerTotalItemCount) && rangeIncludesLastItem) {
            itemCount--;
        }
        return itemCount;
    }

    @Override
    protected void forwardChanged() {
        notifyDataSetChanged();
    }

    @Override
    protected void forwardItemRangeChanged(int innerPositionStart, int innerItemCount) {
        int innerTotalItemCountCurrent = super.getItemCount();
        int outerPositionStart = innerToOuter(innerPositionStart, innerTotalItemCountCurrent);
        int outerItemCount = innerToOuterCount(innerPositionStart, innerItemCount, innerTotalItemCountCurrent);
//        notifyItemRangeChanged(outerPositionStart, outerItemCount);
        notifyDataSetChanged();
    }

    @Override
    protected void forwardItemRangeInserted(int innerPositionStart, int innerItemCount) {
        int innerTotalItemCountPostInsertion = super.getItemCount();
        int innerTotalItemCountPreInsertion = super.getItemCount() - innerItemCount;
        int outerPositionStart = innerToOuter(innerPositionStart, innerTotalItemCountPostInsertion);
        int outerItemCount = innerToOuterCount(innerPositionStart, innerItemCount, innerTotalItemCountPostInsertion);
//        notifyItemRangeInserted(outerPositionStart, outerItemCount);
        notifyDataSetChanged();
    }

    @Override
    protected void forwardItemRangeRemoved(int innerPositionStart, int innerItemCount) {
        int innerTotalItemCountPreRemoval = super.getItemCount() + innerItemCount;
        int innerTotalItemCountPostRemoval = super.getItemCount();
        int outerPositionStart = innerToOuter(innerPositionStart, innerTotalItemCountPreRemoval);
        int outerItemCount = innerToOuterCount(innerPositionStart, innerItemCount, innerTotalItemCountPreRemoval);
//        notifyItemRangeRemoved(outerPositionStart, outerItemCount);
        notifyDataSetChanged();
    }

    @Override
    protected void forwardItemRangeMoved(int innerFromPosition, int innerToPosition, int innerItemCount) {
        // TODO: forwardItemRangeMoved
//        notifyItemRangeMoved(innerToOuter(innerFromPosition), innerToOuter(innerToPosition), innerItemCount);
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
