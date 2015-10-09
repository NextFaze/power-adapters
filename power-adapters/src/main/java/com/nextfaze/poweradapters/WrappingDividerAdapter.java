package com.nextfaze.poweradapters;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.WeakHashMap;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

@Accessors(prefix = "m")
final class WrappingDividerAdapter extends PowerAdapterWrapper {

    @NonNull
    private final WeakHashMap<ViewType, ViewTypeWrapper> mViewTypes = new WeakHashMap<>();

    @NonNull
    private final WeakHashMap<ViewGroup, DividerViewHolder> mViewMetadata = new WeakHashMap<>();

    @NonNull
    private final DividerAdapterBuilder.EmptyPolicy mEmptyPolicy;

    @Nullable
    private final Item mLeadingItem;

    @Nullable
    private final Item mTrailingItem;

    @Nullable
    private final Item mInnerItem;

    WrappingDividerAdapter(@NonNull PowerAdapter adapter,
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
    public int getItemCount() {
        return getItemCount(super.getItemCount());
    }

    private int getItemCount(final int innerItemCount) {
        int itemCount = innerItemCount;
        if (itemCount == 0) {
            if (isLeadingVisible(innerItemCount)) {
                itemCount++;
            }
            if (isTrailingVisible(innerItemCount)) {
                itemCount++;
            }
        }
        return itemCount;
    }

    @NonNull
    @Override
    public ViewType getItemViewType(int position) {
        if (super.getItemCount() == 0) {
            if (isLeadingVisible() && position == 0) {
                //noinspection ConstantConditions
                return mLeadingItem;
            }
            if (isTrailingVisible() && position == getItemCount() - 1) {
                //noinspection ConstantConditions
                return mTrailingItem;
            }
        }
        ViewType innerViewType = super.getItemViewType(position);
        ViewTypeWrapper viewTypeWrapper = mViewTypes.get(innerViewType);
        if (viewTypeWrapper == null) {
            viewTypeWrapper = new ViewTypeWrapper();
            mViewTypes.put(innerViewType, viewTypeWrapper);
        }
        viewTypeWrapper.viewType = innerViewType;
        return viewTypeWrapper;
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        if (viewType == mLeadingItem) {
            return mLeadingItem.create(parent);
        }
        if (viewType == mTrailingItem) {
            return mTrailingItem.create(parent);
        }
        ViewTypeWrapper viewTypeWrapper = (ViewTypeWrapper) viewType;
        // Must inflate in order to get the correct layout params.
        ViewGroup viewGroup = (ViewGroup) layoutInflater(parent)
                .inflate(R.layout.power_adapters_divider_adapter_wrapper, parent, false);
        View childView = super.newView(viewGroup, viewTypeWrapper.viewType);
        mViewMetadata.put(viewGroup, new DividerViewHolder(viewGroup, childView));
        return viewGroup;
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        int position = holder.getPosition();
        if (super.getItemCount() == 0) {
            if (isLeadingVisible() || isTrailingVisible()) {
                return;
            }
        }
        ViewGroup viewGroup = (ViewGroup) view;
        DividerViewHolder dividerViewHolder = mViewMetadata.get(viewGroup);
        dividerViewHolder.updateDividers(position);
        super.bindView(dividerViewHolder.mChildView, holder);
    }

    @Override
    public boolean isEnabled(int position) {
        if (super.getItemCount() == 0) {
            if (isLeadingVisible() && position == 0) {
                return false;
            }
            if (isTrailingVisible() && position == getItemCount() - 1) {
                return false;
            }
        }
        return super.isEnabled(position);
    }

    @Override
    public long getItemId(int position) {
        if (super.getItemCount() == 0) {
            if (isLeadingVisible() && position == 0) {
                return NO_ID;
            }
            if (isTrailingVisible() && position == getItemCount() - 1) {
                return NO_ID;
            }
        }
        return super.getItemId(position);
    }

    private boolean isLeadingVisible() {
        return isLeadingVisible(super.getItemCount());
    }

    private boolean isLeadingVisible(int itemCount) {
        return mLeadingItem != null && mEmptyPolicy.shouldShowLeading(itemCount);
    }

    private boolean isTrailingVisible() {
        return isTrailingVisible(super.getItemCount());
    }

    private boolean isTrailingVisible(int itemCount) {
        return mTrailingItem != null && mEmptyPolicy.shouldShowTrailing(itemCount);
    }

    private boolean isInnerVisible() {
        int itemCount = getItemCount();
        // Inner dividers only present if there's at least 2 items.
        return itemCount > 1 && mInnerItem != null && mEmptyPolicy.shouldShowInner(itemCount);
    }

    private static final class ViewTypeWrapper implements ViewType {
        @NonNull
        private ViewType viewType;
    }

    private final class DividerViewHolder {

        @NonNull
        private final View mChildView;

        @Nullable
        private final View mLeadingView;

        @Nullable
        private final View mInnerView;

        @Nullable
        private final View mTrailingView;

        private DividerViewHolder(@NonNull ViewGroup viewGroup, @NonNull View childView) {
            mChildView = childView;
            mLeadingView = mLeadingItem != null ? mLeadingItem.create(viewGroup) : null;
            if (mLeadingView != null) {
                viewGroup.addView(mLeadingView);
            }
            viewGroup.addView(childView);
            mInnerView = mInnerItem != null ? mInnerItem.create(viewGroup) : null;
            if (mInnerView != null) {
                viewGroup.addView(mInnerView);
            }
            mTrailingView = mTrailingItem != null ? mTrailingItem.create(viewGroup) : null;
            if (mTrailingView != null) {
                viewGroup.addView(mTrailingView);
            }
        }

        void updateDividers(int position) {
            if (mLeadingView != null) {
                mLeadingView.setVisibility(position == 0 && isLeadingVisible() ? VISIBLE : GONE);
            }
            if (mInnerView != null) {
                mInnerView.setVisibility((position >= 0 && position < getItemCount() - 1 && isInnerVisible()) ? VISIBLE : GONE);
            }
            if (mTrailingView != null) {
                mTrailingView.setVisibility(position == getItemCount() - 1 && isTrailingVisible() ? VISIBLE : GONE);
            }
        }
    }

    @Override
    protected void forwardItemRangeInserted(int innerPositionStart, int innerItemCount) {
        final int innerTotalCountBefore = super.getItemCount() - innerItemCount;
        final int innerTotalCountAfter = super.getItemCount();
        final boolean rangeIncludesFirstItem = innerPositionStart == 0;
        final boolean rangeIncludesLastItem = innerPositionStart + innerItemCount >= innerTotalCountAfter;

        // Became non-empty?
        if (innerTotalCountBefore == 0 && innerTotalCountAfter > 0) {
            // Remove single leading.
            if (isLeadingVisible(innerTotalCountBefore)) {
                notifyItemRemoved(0);
            }

            // Remove single trailing.
            if (isTrailingVisible(innerTotalCountBefore)) {
                notifyItemRemoved(getItemCount(innerTotalCountBefore) - 1);
            }
        }

        super.forwardItemRangeInserted(innerPositionStart, innerItemCount);

        // Leading might turn into an inner.
        if (rangeIncludesFirstItem && innerTotalCountBefore > 0 && innerTotalCountAfter > 1) {
            notifyItemChanged(innerPositionStart + innerItemCount);
        }

        // Trailing might turn into an inner.
        if (rangeIncludesLastItem && innerTotalCountBefore > 0 && innerTotalCountAfter > 1) {
            notifyItemChanged(innerPositionStart - 1);
        }
    }

    @Override
    protected void forwardItemRangeRemoved(int innerPositionStart, int innerItemCount) {
        final int innerTotalCountBefore = super.getItemCount() + innerItemCount;
        final int innerTotalCountAfter = super.getItemCount();
        final boolean rangeIncludesFirstItem = innerPositionStart == 0;
        final boolean rangeIncludesLastItem = innerPositionStart + innerItemCount >= innerTotalCountBefore;

        super.forwardItemRangeRemoved(innerPositionStart, innerItemCount);

        // Became empty?
        if (innerTotalCountBefore > 0 && innerTotalCountAfter == 0) {
            // Add single leading.
            if (isLeadingVisible(innerTotalCountAfter)) {
                notifyItemInserted(0);
            }

            // Add single trailing.
            if (isTrailingVisible(innerTotalCountAfter)) {
                notifyItemInserted(getItemCount(innerTotalCountAfter) - 1);
            }
        }

        // Inner might turn into a leading.
        if (rangeIncludesFirstItem && innerTotalCountAfter > 0 && innerTotalCountBefore > 1) {
            notifyItemChanged(0);
        }

        // Inner might turn into a trailing.
        if (rangeIncludesLastItem && innerTotalCountAfter > 0 && innerTotalCountBefore > 1) {
            notifyItemChanged(getItemCount() - 1);
        }
    }
}
