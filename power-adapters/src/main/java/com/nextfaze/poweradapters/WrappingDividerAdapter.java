package com.nextfaze.poweradapters;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.nextfaze.poweradapters.internal.WeakMap;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

/** Implements dividers by wrapping each item in another view, which then decorates the item with dividers. */
final class WrappingDividerAdapter extends PowerAdapterWrapper {

    @NonNull
    private final WeakMap<Object, ViewTypeWrapper> mViewTypes = new WeakMap<>();

    @NonNull
    private final DividerAdapterBuilder.EmptyPolicy mEmptyPolicy;

    @Nullable
    final Item mLeadingItem;

    @Nullable
    final Item mTrailingItem;

    @Nullable
    final Item mInnerItem;

    private int mItemCount;

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
        if (getObserverCount() > 0) {
            return mItemCount;
        }
        return getItemCount(super.getItemCount());
    }

    int getItemCount(final int innerItemCount) {
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
    public Object getItemViewType(int position) {
        int innerItemCount = super.getItemCount();
        if (innerItemCount == 0) {
            if (isLeadingVisible(innerItemCount) && position == 0) {
                //noinspection ConstantConditions
                return mLeadingItem;
            }
            if (isTrailingVisible(innerItemCount) && position == getItemCount(innerItemCount) - 1) {
                //noinspection ConstantConditions
                return mTrailingItem;
            }
        }
        Object innerViewType = super.getItemViewType(position);
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
    public View newView(@NonNull ViewGroup parent, @NonNull Object viewType) {
        if (viewType == mLeadingItem) {
            return mLeadingItem.create(parent);
        }
        if (viewType == mTrailingItem) {
            return mTrailingItem.create(parent);
        }
        ViewTypeWrapper viewTypeWrapper = (ViewTypeWrapper) viewType;
        // Must inflate in order to get the correct layout params.
        DividerLinearLayout viewGroup = (DividerLinearLayout) layoutInflater(parent)
                .inflate(R.layout.power_adapters_divider_adapter_wrapper, parent, false);
        View childView = super.newView(viewGroup, viewTypeWrapper.viewType);
        viewGroup.viewHolder = new DividerViewHolder(viewGroup, childView);
        return viewGroup;
    }

    @Override
    public void bindView(
            @NonNull Container container,
            @NonNull View view,
            @NonNull Holder holder,
            @NonNull List<Object> payloads
    ) {
        int position = holder.getPosition();
        int innerItemCount = super.getItemCount();
        if (innerItemCount == 0 && (isLeadingVisible(innerItemCount) || isTrailingVisible(innerItemCount))) {
            return;
        }
        DividerLinearLayout viewGroup = (DividerLinearLayout) view;
        DividerViewHolder dividerViewHolder = viewGroup.viewHolder;
        dividerViewHolder.updateDividers(position);
        super.bindView(container, dividerViewHolder.mChildView, holder, payloads);
    }

    @Override
    public boolean isEnabled(int position) {
        int innerItemCount = super.getItemCount();
        if (innerItemCount == 0) {
            if (isLeadingVisible(innerItemCount) && position == 0) {
                return false;
            }
            if (isTrailingVisible(innerItemCount) && position == getItemCount(innerItemCount) - 1) {
                return false;
            }
        }
        return super.isEnabled(position);
    }

    @Override
    public long getItemId(int position) {
        int innerItemCount = super.getItemCount();
        if (innerItemCount == 0) {
            if (isLeadingVisible(innerItemCount) && position == 0) {
                return NO_ID;
            }
            if (isTrailingVisible(innerItemCount) && position == getItemCount(innerItemCount) - 1) {
                return NO_ID;
            }
        }
        return super.getItemId(position);
    }

    boolean isLeadingVisible(int innerItemCount) {
        return mLeadingItem != null && mEmptyPolicy.shouldShowLeading(innerItemCount);
    }

    boolean isTrailingVisible(int innerItemCount) {
        return mTrailingItem != null && mEmptyPolicy.shouldShowTrailing(innerItemCount);
    }

    boolean isInnerVisible(int innerItemCount) {
        int itemCount = getItemCount(innerItemCount);
        // Inner dividers only present if there's at least 2 items.
        return itemCount > 1 && mInnerItem != null && mEmptyPolicy.shouldShowInner(itemCount);
    }

    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        updateItemCount(super.getItemCount());
    }

    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mItemCount = 0;
    }

    @Override
    protected void forwardChanged() {
        updateItemCount();
        notifyDataSetChanged();
    }

    @Override
    protected void forwardItemRangeInserted(int innerPositionStart, int innerItemCount) {
        final int innerTotalCountAfter = super.getItemCount();
        final int innerTotalCountBefore = innerTotalCountAfter - innerItemCount;
        final boolean rangeIncludesFirstItem = innerPositionStart == 0;
        final boolean rangeIncludesLastItem = innerPositionStart + innerItemCount >= innerTotalCountAfter;

        setItemCount(getItemCount(innerTotalCountBefore));

        // Became non-empty?
        if (innerTotalCountBefore == 0 && innerTotalCountAfter > 0) {
            // Remove single leading.
            if (isLeadingVisible(innerTotalCountBefore)) {
                adjustItemCount(-1);
                notifyItemRemoved(0);
            }

            // Remove single trailing.
            if (isTrailingVisible(innerTotalCountBefore)) {
                adjustItemCount(-1);
                notifyItemRemoved(getItemCount(innerTotalCountBefore) - 1);
            }
        }

        setItemCount(innerTotalCountAfter);
        super.forwardItemRangeInserted(innerPositionStart, innerItemCount);

        // Leading might turn into an inner.
        if (rangeIncludesFirstItem && innerTotalCountBefore > 0 && innerTotalCountAfter > 1) {
            notifyItemChanged(innerPositionStart + innerItemCount, null);
        }

        // Trailing might turn into an inner.
        if (rangeIncludesLastItem && innerTotalCountBefore > 0 && innerTotalCountAfter > 1) {
            notifyItemChanged(innerPositionStart - 1, null);
        }
    }

    @Override
    protected void forwardItemRangeRemoved(int innerPositionStart, int innerItemCount) {
        final int innerTotalCountAfter = super.getItemCount();
        final int innerTotalCountBefore = innerTotalCountAfter + innerItemCount;
        final boolean rangeIncludesFirstItem = innerPositionStart == 0;
        final boolean rangeIncludesLastItem = innerPositionStart + innerItemCount >= innerTotalCountBefore;

        setItemCount(innerTotalCountAfter);
        super.forwardItemRangeRemoved(innerPositionStart, innerItemCount);

        // Became empty?
        if (innerTotalCountBefore > 0 && innerTotalCountAfter == 0) {
            // Add single leading.
            if (isLeadingVisible(innerTotalCountAfter)) {
                adjustItemCount(+1);
                notifyItemInserted(0);
            }

            // Add single trailing.
            if (isTrailingVisible(innerTotalCountAfter)) {
                adjustItemCount(+1);
                notifyItemInserted(getItemCount(innerTotalCountAfter) - 1);
            }
        }

        // Inner might turn into a leading.
        if (rangeIncludesFirstItem && innerTotalCountAfter > 0 && innerTotalCountBefore > 1) {
            notifyItemChanged(0, null);
        }

        // Inner might turn into a trailing.
        if (rangeIncludesLastItem && innerTotalCountAfter > 0 && innerTotalCountBefore > 1) {
            notifyItemChanged(getItemCount(innerTotalCountAfter) - 1, null);
        }
    }

    private void updateItemCount() {
        updateItemCount(super.getItemCount());
    }

    private void updateItemCount(int innerItemCount) {
        mItemCount = getItemCount(innerItemCount);
        validateItemCount();
    }

    private void setItemCount(int itemCount) {
        mItemCount = itemCount;
        validateItemCount();
    }

    private void adjustItemCount(int delta) {
        mItemCount += delta;
        validateItemCount();
    }

    private void validateItemCount() {
        if (mItemCount < 0) {
            throw new AssertionError("Unexpected item count: " + mItemCount);
        }
    }

    private static final class ViewTypeWrapper {
        @NonNull
        Object viewType;

        ViewTypeWrapper() {
        }
    }

    private final class DividerViewHolder {

        @NonNull
        final View mChildView;

        @Nullable
        private final View mLeadingView;

        @Nullable
        private final View mInnerView;

        @Nullable
        private final View mTrailingView;

        DividerViewHolder(@NonNull ViewGroup viewGroup, @NonNull View childView) {
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
            int innerItemCount = WrappingDividerAdapter.super.getItemCount();
            if (mLeadingView != null) {
                mLeadingView.setVisibility(position == 0 && isLeadingVisible(innerItemCount) ? VISIBLE : GONE);
            }
            if (mInnerView != null) {
                mInnerView.setVisibility((position >= 0 && position < getItemCount(innerItemCount) - 1 && isInnerVisible(innerItemCount)) ? VISIBLE : GONE);
            }
            if (mTrailingView != null) {
                mTrailingView.setVisibility(position == getItemCount(innerItemCount) - 1 && isTrailingVisible(innerItemCount) ? VISIBLE : GONE);
            }
        }
    }

    /** For internal use only. */
    @RestrictTo(LIBRARY_GROUP)
    public static final class DividerLinearLayout extends LinearLayout {

        DividerViewHolder viewHolder;

        public DividerLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }
    }
}
