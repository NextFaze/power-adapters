package com.nextfaze.databind;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
public final class DividerAdapter extends ListAdapterWrapper {

    @LayoutRes
    private final int mLeadingItemResource;

    @LayoutRes
    private final int mTrailingItemResource;

    @LayoutRes
    private final int mInnerItemResource;

    private final boolean mShowDividerIfEmpty;

    /** @see ListAdapterWrapper#ListAdapterWrapper(ListAdapter, boolean) */
    DividerAdapter(@NonNull ListAdapter adapter,
                          int leadingItemResource,
                          int trailingItemResource,
                          int innerItemResource,
                          boolean showDividerIfEmpty,
                          boolean takeOwnership) {
        super(adapter, takeOwnership);
        mLeadingItemResource = leadingItemResource;
        mTrailingItemResource = trailingItemResource;
        mInnerItemResource = innerItemResource;
        mShowDividerIfEmpty = showDividerIfEmpty;
    }

    @Override
    public final int getCount() {
        int superCount = super.getCount();
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

    @Override
    public final boolean isEnabled(int position) {
        //noinspection SimplifiableIfStatement
        if (isDivider(position)) {
            return false;
        }
        return super.isEnabled(map(position));
    }

    @Override
    public final Object getItem(int position) {
        if (isDivider(position)) {
            return null;
        }
        return super.getItem(map(position));
    }

    @Override
    public final long getItemId(int position) {
        if (isDivider(position)) {
            return -1;
        }
        return super.getItemId(map(position));
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (isInnerDivider(position)) {
            if (convertView == null) {
                convertView = newInnerDividerView(getLayoutInflater(parent), parent);
            }
            return convertView;
        }
        if (isLeadingDivider(position)) {
            if (convertView == null) {
                convertView = newLeadingDividerView(getLayoutInflater(parent), parent);
            }
            return convertView;
        }
        if (isTrailingDivider(position)) {
            if (convertView == null) {
                convertView = newTrailingDividerView(getLayoutInflater(parent), parent);
            }
            return convertView;
        }
        return super.getView(map(position), convertView, parent);
    }

    @Override
    public final int getItemViewType(int position) {
        if (isInnerDivider(position)) {
            return getInnerDividerItemViewType();
        }
        if (isLeadingDivider(position)) {
            return getLeadingDividerItemViewType();
        }
        if (isTrailingDivider(position)) {
            return getTrailingDividerItemViewType();
        }
        return super.getItemViewType(map(position));
    }

    @Override
    public final int getViewTypeCount() {
        // One additional view type for inner, leading, and trailing divider views.
        return super.getViewTypeCount() + 3;
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

    private boolean isLeadingDivider(int position) {
        return mLeadingItemResource > 0 && position == 0;
    }

    private boolean isTrailingDivider(int position) {
        return mTrailingItemResource > 0 && position == getCount() - 1;
    }

    private int getInnerDividerItemViewType() {
        return super.getViewTypeCount();
    }

    private int getLeadingDividerItemViewType() {
        return super.getViewTypeCount() + 1;
    }

    private int getTrailingDividerItemViewType() {
        return super.getViewTypeCount() + 2;
    }

    private int map(int position) {
        if (mLeadingItemResource > 0) {
            return (position - 1) / 2;
        }
        return position / 2;
    }

    @NonNull
    private static LayoutInflater getLayoutInflater(@NonNull View v) {
        return LayoutInflater.from(v.getContext());
    }

    public static final class Builder {

        @NonNull
        private final ListAdapter mAdapter;

        private int mLeadingItemResource;
        private int mTrailingItemResource;
        private int mInnerItemResource;
        private boolean mShowDividerIfEmpty = true;
        private boolean mTakeOwnership = true;

        public Builder(@NonNull ListAdapter adapter) {
            mAdapter = adapter;
        }

        public boolean isEmpty() {
            return mLeadingItemResource <= 0 && mTrailingItemResource <= 0 && mInnerItemResource <= 0;
        }

        @NonNull
        public Builder leadingItemResource(@LayoutRes int itemResource) {
            mLeadingItemResource = itemResource;
            return this;
        }

        @NonNull
        public Builder trailingItemResource(@LayoutRes int itemResource) {
            mTrailingItemResource = itemResource;
            return this;
        }

        @NonNull
        public Builder innerItemResource(@LayoutRes int itemResource) {
            mInnerItemResource = itemResource;
            return this;
        }

        @NonNull
        public Builder outerItemResource(@LayoutRes int itemResource) {
            return leadingItemResource(itemResource).trailingItemResource(itemResource);
        }

        @NonNull
        public Builder showDividerIfEmpty(boolean showDividerIfEmpty) {
            mShowDividerIfEmpty = showDividerIfEmpty;
            return this;
        }

        /** @see ListAdapterWrapper#ListAdapterWrapper(ListAdapter, boolean) */
        @NonNull
        public Builder takeOwnership(boolean takeOwnership) {
            mTakeOwnership = takeOwnership;
            return this;
        }

        @NonNull
        public DividerAdapter build() {
            return new DividerAdapter(mAdapter, mLeadingItemResource, mTrailingItemResource,
                    mInnerItemResource, mShowDividerIfEmpty, mTakeOwnership);
        }
    }
}
