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

    @NonNull
    @Override
    public ViewType getItemViewType(int position) {
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
        ViewGroup viewGroup = (ViewGroup) view;
        DividerViewHolder dividerViewHolder = mViewMetadata.get(viewGroup);
        dividerViewHolder.updateDividers(holder.getPosition());
        super.bindView(dividerViewHolder.mChildView, holder);
    }

    private static final class ViewTypeWrapper implements ViewType {
        @NonNull
        private ViewType viewType;
    }

    private final class DividerViewHolder {

        @NonNull
        private final View mChildView;

        @NonNull
        private final ViewGroup mViewGroup;

        @Nullable
        private final View mLeadingView;

        @Nullable
        private final View mInnerView;

        @Nullable
        private final View mTrailingView;

        private DividerViewHolder(@NonNull ViewGroup viewGroup, @NonNull View childView) {
            mViewGroup = viewGroup;
            mChildView = childView;
            mLeadingView = mLeadingItem != null ? mLeadingItem.create(viewGroup) : null;
            if (mLeadingView != null) {
                mViewGroup.addView(mLeadingView);
            }
            mViewGroup.addView(childView);
            mInnerView = mInnerItem != null ? mInnerItem.create(viewGroup) : null;
            if (mInnerView != null) {
                mViewGroup.addView(mInnerView);
            }
            mTrailingView = mTrailingItem != null ? mTrailingItem.create(viewGroup) : null;
            if (mTrailingView != null) {
                mViewGroup.addView(mTrailingView);
            }
        }

        void updateDividers(int position) {
            if (mLeadingView != null) {
                mLeadingView.setVisibility(position == 0 ? VISIBLE : GONE);
            }
            if (mInnerView != null) {
                mInnerView.setVisibility((position >= 0 && position < getItemCount() - 1 ) ? VISIBLE : GONE);
            }
            if (mTrailingView != null) {
                mTrailingView.setVisibility(position == getItemCount() - 1 ? VISIBLE : GONE);
            }
        }
    }

    @Override
    protected void forwardItemRangeInserted(int innerPositionStart, int innerItemCount) {
        super.forwardItemRangeInserted(innerPositionStart, innerItemCount);
        final int innerTotalItemCountAfter = super.getItemCount();
        final boolean rangeIncludesFirstItem = innerPositionStart == 0;
        final boolean rangeIncludesLastItem = innerPositionStart + innerItemCount >= innerTotalItemCountAfter;
        if (rangeIncludesFirstItem && innerTotalItemCountAfter > 1) {
            notifyItemChanged(innerPositionStart + innerItemCount);
        }
        if (rangeIncludesLastItem && innerTotalItemCountAfter > 1) {
            notifyItemChanged(innerPositionStart - 1);
        }
    }

    @Override
    protected void forwardItemRangeRemoved(int innerPositionStart, int innerItemCount) {
        super.forwardItemRangeRemoved(innerPositionStart, innerItemCount);
        final int innerTotalItemCountBefore = super.getItemCount() + innerItemCount;
        final boolean rangeIncludesFirstItem = innerPositionStart == 0;
        final boolean rangeIncludesLastItem = innerPositionStart + innerItemCount >= innerTotalItemCountBefore;
        if (rangeIncludesFirstItem && innerTotalItemCountBefore > 1) {
            notifyItemChanged(0);
        }
        if (rangeIncludesLastItem && innerTotalItemCountBefore > 1) {
            notifyItemChanged(getItemCount() - 1);
        }
    }
}
