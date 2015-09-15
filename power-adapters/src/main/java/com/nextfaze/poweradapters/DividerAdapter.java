package com.nextfaze.poweradapters;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
final class DividerAdapter extends PowerAdapterWrapper {

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
        if (mInnerItem != null && isInnerDivider(position)) {
            return mInnerItem;
        }
        if (mLeadingItem != null && isLeadingDivider(position)) {
            return mLeadingItem;
        }
        if (mTrailingItem != null && isTrailingDivider(position)) {
            return mTrailingItem;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        if (viewType == mInnerItem) {
            return mInnerItem.create(parent);
        }
        if (viewType == mLeadingItem) {
            return mLeadingItem.create(parent);
        }
        if (viewType == mTrailingItem) {
            return mTrailingItem.create(parent);
        }
        return super.newView(parent, viewType);
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        ViewType viewType = getItemViewType(holder.getPosition());
        if (viewType == mInnerItem) {
            return;
        }
        if (viewType == mLeadingItem) {
            return;
        }
        if (viewType == mTrailingItem) {
            return;
        }
        super.bindView(view, holder);
    }

    @Override
    public int getItemCount() {
        return getItemCount(super.getItemCount());
    }

    private int getItemCount(int innerItemCount) {
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
        // Trailing dividers only present if there's at least 1 item.
        return itemCount > 0 && mTrailingItem != null && mEmptyPolicy.shouldShowTrailing(itemCount);
    }

    private boolean isInnerVisible() {
        return isInnerVisible(super.getItemCount());
    }

    private boolean isInnerVisible(int itemCount) {
        // Inner dividers only present if there's at least 2 items.
        return itemCount > 1 && mInnerItem != null && mEmptyPolicy.shouldShowInner(itemCount);
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
        return outerPosition;
    }

    @Override
    protected void forwardItemRangeChanged(final int innerPositionStart, final int innerItemCount) {
        int innerTotalItemCount = super.getItemCount();
        for (int i = 0; i < innerItemCount; i++) {
            notifyItemRangeChanged(innerToOuter(innerPositionStart + i, innerTotalItemCount), 1);
        }
    }

    @Override
    protected void forwardItemRangeInserted(final int innerPositionStart, final int innerItemCount) {
        final int innerTotalItemCountBefore = super.getItemCount() - innerItemCount;
        final int innerTotalItemCountAfter = super.getItemCount();
        final boolean rangeIncludesFirstItem = innerPositionStart == 0;
        final boolean rangeIncludesLastItem = innerPositionStart + innerItemCount >= innerTotalItemCountAfter;
        int outerPositionStart = innerPositionStart;
        int outerItemCount = innerItemCount;

        // Advance past leading.
        if (isLeadingVisible(innerTotalItemCountAfter)) {
            outerPositionStart++;
        }

        if (isInnerVisible(innerTotalItemCountAfter)) {
            // Advance past an inner for each item.
            outerPositionStart += innerPositionStart;
            // Add an inner for each item.
            outerItemCount += innerItemCount;
            if (rangeIncludesLastItem) {
                // Final divider would be the trailing, so don't include that in the insertion count.
                outerItemCount--;
            }
        }

        // Notify of middle range inserted.
//        notifyItemRangeInserted(outerPositionStart, outerItemCount);

        if (rangeIncludesLastItem) {
            // Account for trailing if last item being inserted. We only insert another item if inners are present.
            if (isTrailingVisible(innerTotalItemCountAfter) && isInnerVisible(innerTotalItemCountAfter)) {
//                notifyItemInserted(getItemCount() - 1);
                outerItemCount++;
            }
        }

        notifyItemRangeInserted(outerPositionStart, outerItemCount);
    }

    @Override
    protected void forwardItemRangeRemoved(final int innerPositionStart, final int innerItemCount) {
        final int innerTotalItemCountBefore = super.getItemCount() + innerItemCount;
        final int innerTotalItemCountAfter = super.getItemCount();
        final boolean rangeIncludesFirstItem = innerPositionStart == 0;
        final boolean rangeIncludesLastItem = innerPositionStart + innerItemCount >= innerTotalItemCountBefore;
        int outerPositionStart = innerPositionStart;
        int outerItemCount = innerItemCount;

        // NOTE: Notifications must be issued highest-to-lowest, otherwise visual glitches occur.

        // Advance past leading.
        if (isLeadingVisible(innerTotalItemCountBefore)) {
            outerPositionStart++;
        }

        if (isInnerVisible(innerTotalItemCountBefore)) {
            // Advance past an inner for each item.
            outerPositionStart += innerPositionStart;
            // Add an inner for each item.
            outerItemCount += innerItemCount;
            if (rangeIncludesLastItem) {
                // Final divider would be the trailing, so don't include that in the removal count.
                outerItemCount--;
                if (!isTrailingVisible(innerTotalItemCountBefore)) {
                    // No trailing, meaning this is the last inner divider and should be removed.
                    outerPositionStart--;
                    outerItemCount++;
                }
            }
        }

        if (rangeIncludesLastItem) {
            if (isTrailingVisible(innerTotalItemCountBefore)) {
                // Trailing was removed.
//                notifyItemRemoved(getItemCount(innerTotalItemCountBefore) - 1);
                outerItemCount++;
                if (isInnerVisible(innerTotalItemCountBefore)) {
                    // Inner changed into a trailing.
                    notifyItemChanged(outerPositionStart - 1);
                } else if (innerTotalItemCountAfter > 0) {
                    // A new trailing was inserted at the end.
                    notifyItemInserted(outerPositionStart);
                }
            }
        }

        // Notify of middle range removed.
//        notifyItemRangeRemoved(outerPositionStart, outerItemCount);

        if (rangeIncludesFirstItem) {
            // The only change leading can undergo from a removal is becoming invisible.
            if (didLeadingBecomeInvisible(innerTotalItemCountBefore, innerTotalItemCountAfter)) {
//                notifyItemRemoved(0);
                outerPositionStart--;
                outerItemCount++;
            }
        }

        notifyItemRangeRemoved(outerPositionStart, outerItemCount);
    }

    @Override
    protected void forwardItemRangeMoved(final int innerFromPosition,
                                         final int innerToPosition,
                                         final int innerItemCount) {
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
