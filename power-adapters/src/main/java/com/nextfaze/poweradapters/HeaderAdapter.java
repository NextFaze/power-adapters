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
 * Wraps an existing {@link ListAdapter} to provide header views above the wrapped adapters items. This class can be
 * subclassed for greater control over the presence of header views.
 */
public abstract class HeaderAdapter extends PowerAdapterWrapper {

    protected HeaderAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
    }

    @NonNull
    protected abstract View getHeaderView(@NonNull LayoutInflater layoutInflater,
                                          @NonNull ViewGroup parent,
                                          int position);

    protected abstract int getHeaderCount(boolean visibleOnly);

    @Override
    public final int getItemCount() {
        return getHeaderCount(true) + super.getItemCount();
    }

    @Override
    public final long getItemId(int position) {
        if (isHeaderView(position)) {
            return NO_ID;
        }
        return super.getItemId(outerToInnerPosition(position));
    }

    @Override
    public final int getViewTypeCount() {
        return super.getViewTypeCount() + getHeaderCount(false);
    }

    @Override
    public final int getItemViewType(int position) {
        int itemViewType = headerItemViewType(position);
        if (itemViewType != -1) {
            return itemViewType;
        }
        return super.getItemViewType(outerToInnerPosition(position));
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, int itemViewType) {
        int headerIndex = itemViewTypeToHeaderIndex(itemViewType);
        if (headerIndex != -1) {
            return getHeaderView(layoutInflater(parent), parent, headerIndex);
        }
        return super.newView(parent, itemViewType);
    }

    @Override
    public void bindView(@NonNull View view, int position) {
        if (!isHeaderView(position)) {
            super.bindView(view, outerToInnerPosition(position));
        }
    }

    private boolean isHeaderView(int position) {
        return position < getHeaderCount(true);
    }

    private int headerItemViewType(int position) {
        if (!isHeaderView(position)) {
            return -1;
        }
        return super.getViewTypeCount() + position;
    }

    private int itemViewTypeToHeaderIndex(int itemViewType) {
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
        return position - getHeaderCount(true);
    }

    public static final class Builder {

        @NonNull
        private final PowerAdapter mAdapter;

        @NonNull
        private final ArrayList<Item> mHeaders = new ArrayList<>();

        private boolean mHideHeadersIfEmpty;

        public Builder(@NonNull PowerAdapter adapter) {
            mAdapter = adapter;
        }

        @NonNull
        public Builder headerView(@NonNull View headerView) {
            mHeaders.add(new Item(headerView));
            return this;
        }

        @NonNull
        public Builder headerResource(@LayoutRes int headerResource) {
            mHeaders.add(new Item(headerResource));
            return this;
        }

        @NonNull
        public Builder hideHeadersIfEmpty(boolean hideHeadersIfEmpty) {
            mHideHeadersIfEmpty = hideHeadersIfEmpty;
            return this;
        }

        @NonNull
        public HeaderAdapter build() {
            return new Impl(mAdapter, mHeaders, mHideHeadersIfEmpty);
        }
    }

    private static final class Impl extends HeaderAdapter {

        @NonNull
        private final ArrayList<Item> mHeaders = new ArrayList<>();

        private boolean mHideHeadersIfEmpty;

        Impl(@NonNull PowerAdapter adapter, @NonNull List<Item> headers, boolean hideHeadersIfEmpty) {
            super(adapter);
            mHeaders.addAll(headers);
            mHideHeadersIfEmpty = hideHeadersIfEmpty;
        }

        @NonNull
        @Override
        protected View getHeaderView(@NonNull LayoutInflater layoutInflater,
                                     @NonNull ViewGroup parent,
                                     int position) {
            return mHeaders.get(position).get(layoutInflater, parent);
        }

        @Override
        protected int getHeaderCount(boolean visibleOnly) {
            if (visibleOnly && mHideHeadersIfEmpty && isUnderlyingAdapterEmpty()) {
                return 0;
            }
            return mHeaders.size();
        }

        private boolean isUnderlyingAdapterEmpty() {
            return getAdapter().getItemCount() == 0;
        }
    }
}
