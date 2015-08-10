package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;
import lombok.experimental.Accessors;

import static com.nextfaze.poweradapters.AdapterUtils.layoutInflater;

@Accessors(prefix = "m")
public final class DividerAdapter extends PowerAdapterWrapper {

    private static final int ITEM_VIEW_TYPE_INNER = 0;
    private static final int ITEM_VIEW_TYPE_LEADING = 1;
    private static final int ITEM_VIEW_TYPE_TRAILING = 2;
    private static final int ITEM_VIEW_TYPE_TOTAL = 3;

    @LayoutRes
    private final int mLeadingItemResource;

    @LayoutRes
    private final int mTrailingItemResource;

    @LayoutRes
    private final int mInnerItemResource;

    // TODO: Use a policy enum instead. Maybe a policy interface + enum?
    private final boolean mShowDividerIfEmpty;

    DividerAdapter(@NonNull PowerAdapter adapter,
                   int leadingItemResource,
                   int trailingItemResource,
                   int innerItemResource,
                   boolean showDividerIfEmpty) {
        super(adapter);
        mLeadingItemResource = leadingItemResource;
        mTrailingItemResource = trailingItemResource;
        mInnerItemResource = innerItemResource;
        mShowDividerIfEmpty = showDividerIfEmpty;
    }

    @Override
    public final int getItemCount() {
        int superCount = super.getItemCount();
        if (!mShowDividerIfEmpty && superCount <= 0) {
            return 0;
        }
        int count;
        if (mInnerItemResource > 0) {
            count = superCount * 2 - 1;
        } else {
            count = superCount;
        }
        if (mLeadingItemResource > 0) {
            ++count;
        }
        if (mTrailingItemResource > 0) {
            ++count;
        }
        return count;
    }

//    @Override
//    public final boolean isEnabled(int position) {
//        //noinspection SimplifiableIfStatement
//        if (isDivider(position)) {
//            return false;
//        }
//        return super.isEnabled(map(position));
//    }

//    @Override
//    public final Object getItem(int position) {
//        if (isDivider(position)) {
//            return null;
//        }
//        return super.getItem(map(position));
//    }

    @Override
    public final long getItemId(int position) {
        if (isDivider(position)) {
            return NO_ID;
        }
        return super.getItemId(map(position));
    }

    @Override
    public final int getItemViewType(int position) {
        if (isInnerDivider(position)) {
            return innerDividerItemViewType();
        }
        if (isLeadingDivider(position)) {
            return leadingDividerItemViewType();
        }
        if (isTrailingDivider(position)) {
            return trailingDividerItemViewType();
        }
        return super.getItemViewType(map(position));
    }

    @Override
    public final int getViewTypeCount() {
        return super.getViewTypeCount() + ITEM_VIEW_TYPE_TOTAL;
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, int itemViewType) {
        if (itemViewType == innerDividerItemViewType()) {
            return newInnerDividerView(layoutInflater(parent), parent);
        }
        if (itemViewType == leadingDividerItemViewType()) {
            return newLeadingDividerView(layoutInflater(parent), parent);
        }
        if (itemViewType == trailingDividerItemViewType()) {
            return newTrailingDividerView(layoutInflater(parent), parent);
        }
        return super.newView(parent, itemViewType);
    }

    @Override
    public void bindView(@NonNull View view, int position) {
        int itemViewType = getItemViewType(position);
        if (itemViewType == innerDividerItemViewType()) {
            return;
        }
        if (itemViewType == leadingDividerItemViewType()) {
            return;
        }
        if (itemViewType == trailingDividerItemViewType()) {
            return;
        }
        super.bindView(view, position);
    }

    @NonNull
    private View newLeadingDividerView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        return layoutInflater.inflate(mLeadingItemResource, parent, false);
    }

    @NonNull
    private View newTrailingDividerView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        return layoutInflater.inflate(mTrailingItemResource, parent, false);
    }

    @NonNull
    private View newInnerDividerView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        return layoutInflater.inflate(mInnerItemResource, parent, false);
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
        if (mLeadingItemResource <= 0) {
            ++position;
        }
        //noinspection SimplifiableIfStatement
        if (mInnerItemResource > 0) {
            return position % 2 == 0;
        }
        return false;
    }

    private int innerDividerItemViewType() {
        return super.getViewTypeCount() + ITEM_VIEW_TYPE_INNER;
    }

    private boolean isLeadingDivider(int position) {
        return mLeadingItemResource > 0 && position == 0;
    }

    private int leadingDividerItemViewType() {
        return super.getViewTypeCount() + ITEM_VIEW_TYPE_LEADING;
    }

    private boolean isTrailingDivider(int position) {
        return mTrailingItemResource > 0 && position == getItemCount() - 1;
    }

    private int trailingDividerItemViewType() {
        return super.getViewTypeCount() + ITEM_VIEW_TYPE_TRAILING;
    }

    private int map(int position) {
        if (mLeadingItemResource > 0) {
            return (position - 1) / 2;
        }
        return position / 2;
    }

    public static final class Builder {

        @NonNull
        private final PowerAdapter mAdapter;

        private int mLeadingItemResource;
        private int mTrailingItemResource;
        private int mInnerItemResource;
        private boolean mShowDividerIfEmpty = true;

        public Builder(@NonNull PowerAdapter adapter) {
            mAdapter = adapter;
        }

        /** Indicates if any dividers were configured in this builder. */
        public boolean isEmpty() {
            return mLeadingItemResource <= 0 && mTrailingItemResource <= 0 && mInnerItemResource <= 0;
        }

        /** Sets layout resource of the divider that appears BEFORE the wrapped adapters items. */
        @NonNull
        public Builder leadingItemResource(@LayoutRes int itemResource) {
            mLeadingItemResource = itemResource;
            return this;
        }

        /** Sets layout resource of the divider that appears AFTER the wrapped adapters items. */
        @NonNull
        public Builder trailingItemResource(@LayoutRes int itemResource) {
            mTrailingItemResource = itemResource;
            return this;
        }

        /** Sets the layout resource of the divider that appears between all of the wrapped adapters items. */
        @NonNull
        public Builder innerItemResource(@LayoutRes int itemResource) {
            mInnerItemResource = itemResource;
            return this;
        }

        /** Sets layout resource of the divider that appears BEFORE and AFTER the wrapped adapters items. */
        @NonNull
        public Builder outerItemResource(@LayoutRes int itemResource) {
            return leadingItemResource(itemResource).trailingItemResource(itemResource);
        }

        /** If {@code true}, a single divider may be shown if the wrapped adapter is empty. Defaults to {@code true}. */
        @NonNull
        public Builder showDividerIfEmpty(boolean showDividerIfEmpty) {
            mShowDividerIfEmpty = showDividerIfEmpty;
            return this;
        }

        @NonNull
        public DividerAdapter build() {
            return new DividerAdapter(mAdapter, mLeadingItemResource, mTrailingItemResource,
                    mInnerItemResource, mShowDividerIfEmpty);
        }
    }
}
