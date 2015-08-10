package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

import static com.nextfaze.poweradapters.AdapterUtils.layoutInflater;

/**
 * Wraps an existing {@link ListAdapter} to provide footer views below the wrapped adapters items. This class can be
 * subclassed for greater control over the presence of footer views.
 */
public abstract class FooterAdapter extends PowerAdapterWrapper {

    protected FooterAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
    }

    @NonNull
    protected abstract View getFooterView(@NonNull LayoutInflater layoutInflater,
                                          @NonNull ViewGroup parent,
                                          int position);

    protected abstract int getFooterCount(boolean visibleOnly);

    @Override
    public final int getItemCount() {
        return getFooterCount(true) + super.getItemCount();
    }

    @Override
    public final long getItemId(int position) {
        if (isFooterView(position)) {
            return NO_ID;
        }
        return super.getItemId(outerToInnerPosition(position));
    }

    @Override
    public final int getViewTypeCount() {
        return super.getViewTypeCount() + getFooterCount(false);
    }

    @Override
    public final int getItemViewType(int position) {
        int itemViewType = footerItemViewType(position);
        if (itemViewType != -1) {
            return itemViewType;
        }
        return super.getItemViewType(outerToInnerPosition(position));
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, int itemViewType) {
        int footerIndex = itemViewTypeToFooterIndex(itemViewType);
        if (footerIndex != -1) {
            return getFooterView(layoutInflater(parent), parent, footerIndex);
        }
        return super.newView(parent, itemViewType);
    }

    @Override
    public void bindView(@NonNull View view, int position) {
        if (!isFooterView(position)) {
            super.bindView(view, outerToInnerPosition(position));
        }
    }

    private boolean isFooterView(int position) {
        return position >= super.getItemCount();
    }

    private int footerItemViewType(int position) {
        if (!isFooterView(position)) {
            return -1;
        }
        return super.getViewTypeCount() + position - super.getItemCount();
    }

    private int itemViewTypeToFooterIndex(int itemViewType) {
        int superViewTypeCount = super.getViewTypeCount();
        if (itemViewType < superViewTypeCount) {
            return -1;
        }
        if (itemViewType >= getViewTypeCount()) {
            return -1;
        }
        return superViewTypeCount - itemViewType;
    }

    /** Translate a position from our coordinate space to the wrapped adapters coordinate space. */
    private int outerToInnerPosition(int position) {
        return position;
    }

    public static final class Builder {

        @NonNull
        private final PowerAdapter mAdapter;

        @NonNull
        private final ArrayList<Item> mFooters = new ArrayList<>();

        private boolean mHideIfEmpty;

        public Builder(@NonNull PowerAdapter adapter) {
            mAdapter = adapter;
        }

        @NonNull
        public Builder footerView(@NonNull View footerView) {
            mFooters.add(new Item(footerView));
            return this;
        }

        @NonNull
        public Builder footerResource(@LayoutRes int footerResource) {
            mFooters.add(new Item(footerResource));
            return this;
        }

        @NonNull
        public Builder hideIfEmpty(boolean hideIfEmpty) {
            mHideIfEmpty = hideIfEmpty;
            return this;
        }

        @NonNull
        public FooterAdapter build() {
            return new Impl(mAdapter, mFooters, mHideIfEmpty);
        }
    }

    private static final class Impl extends FooterAdapter {

        @NonNull
        private final ArrayList<Item> mFooters = new ArrayList<>();

        private boolean mHideIfEmpty;

        Impl(@NonNull PowerAdapter adapter, @NonNull List<Item> footers, boolean hideIfEmpty) {
            super(adapter);
            mFooters.addAll(footers);
            mHideIfEmpty = hideIfEmpty;
        }

        @NonNull
        @Override
        protected View getFooterView(@NonNull LayoutInflater layoutInflater,
                                     @NonNull ViewGroup parent,
                                     int position) {
            return mFooters.get(position).get(layoutInflater, parent);
        }

        @Override
        protected int getFooterCount(boolean visibleOnly) {
            if (visibleOnly && mHideIfEmpty && isUnderlyingAdapterEmpty()) {
                return 0;
            }
            return mFooters.size();
        }

        private boolean isUnderlyingAdapterEmpty() {
            return getAdapter().getItemCount() == 0;
        }
    }
}
