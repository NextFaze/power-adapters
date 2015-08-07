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
 * Wraps an existing {@link android.widget.ListAdapter} to provide header and footer views above and below the wrapped
 * adapters items
 * respectively. This class can be subclassed for greater control over the presence of header and footer views.
 */
public abstract class HeaderFooterAdapter extends ListAdapterWrapper {

    protected HeaderFooterAdapter(@NonNull ListAdapter adapter) {
        super(adapter);
    }

    @NonNull
    protected abstract View getHeaderView(@NonNull LayoutInflater layoutInflater,
                                          @NonNull ViewGroup parent,
                                          int position);

    protected abstract int getHeaderCount(boolean visibleOnly);

    @NonNull
    protected abstract View getFooterView(@NonNull LayoutInflater layoutInflater,
                                          @NonNull ViewGroup parent,
                                          int position);

    protected abstract int getFooterCount(boolean visibleOnly);

    @Override
    public final int getCount() {
        return getHeaderCount(true) + super.getCount() + getFooterCount(true);
    }

    @Override
    public final boolean isEnabled(int position) {
        //noinspection SimplifiableIfStatement
        if (isHeaderView(position)) {
            return false;
        }
        //noinspection SimplifiableIfStatement
        if (isFooterView(position)) {
            return false;
        }
        return super.isEnabled(outerToInnerPosition(position));
    }

    @Override
    public final long getItemId(int position) {
        if (isHeaderView(position)) {
            return -1;
        }
        if (isFooterView(position)) {
            return -1;
        }
        return super.getItemId(outerToInnerPosition(position));
    }

    @Override
    public final Object getItem(int position) {
        if (isHeaderView(position)) {
            return null;
        }
        if (isFooterView(position)) {
            return null;
        }
        return super.getItem(outerToInnerPosition(position));
    }

    @Override
    public final int getViewTypeCount() {
        return super.getViewTypeCount() + getHeaderCount(false) + getFooterCount(false);
    }

    @Override
    public final int getItemViewType(int position) {
        if (isHeaderView(position)) {
            return super.getViewTypeCount() + headerViewIndex(position);
        }
        if (isFooterView(position)) {
            return super.getViewTypeCount() + getHeaderCount(true) + footerViewIndex(position);
        }
        return super.getItemViewType(outerToInnerPosition(position));
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (isHeaderView(position)) {
            if (convertView == null) {
                convertView = getHeaderView(layoutInflater(parent), parent, headerViewIndex(position));
            }
            return convertView;
        }
        if (isFooterView(position)) {
            if (convertView == null) {
                convertView = getFooterView(layoutInflater(parent), parent, footerViewIndex(position));
            }
            return convertView;
        }
        return super.getView(outerToInnerPosition(position), convertView, parent);
    }

    private boolean isHeaderView(int position) {
        return position < getHeaderCount(true);
    }

    private boolean isFooterView(int position) {
        return position >= super.getCount() + getHeaderCount(true);
    }

    private int headerViewIndex(int position) {
        if (!isHeaderView(position)) {
            return -1;
        }
        return position;
    }

    private int footerViewIndex(int position) {
        if (!isFooterView(position)) {
            return -1;
        }
        return position - super.getCount() - getHeaderCount(false);
    }

    /** Translate a position from our coordinate space to the wrapped adapters coordinate space. */
    private int outerToInnerPosition(int position) {
        return position - getHeaderCount(true);
    }

    public static final class Builder {

        @NonNull
        private final ListAdapter mAdapter;

        @NonNull
        private final ArrayList<Item> mHeaders = new ArrayList<>();

        @NonNull
        private final ArrayList<Item> mFooters = new ArrayList<>();

        private boolean mHideHeadersIfEmpty;
        private boolean mHideFootersIfEmpty;

        public Builder(@NonNull ListAdapter adapter) {
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
        public Builder hideHeadersIfEmpty(boolean hideHeadersIfEmpty) {
            mHideHeadersIfEmpty = hideHeadersIfEmpty;
            return this;
        }

        @NonNull
        public Builder hideFootersIfEmpty(boolean hideFootersIfEmpty) {
            mHideFootersIfEmpty = hideFootersIfEmpty;
            return this;
        }

        @NonNull
        public HeaderFooterAdapter build() {
            return new ArrayHeaderFooterAdapter(mAdapter, mHeaders, mFooters,
                    mHideHeadersIfEmpty, mHideFootersIfEmpty);
        }
    }

    private static final class ArrayHeaderFooterAdapter extends HeaderFooterAdapter {

        @NonNull
        private final ArrayList<Item> mHeaders = new ArrayList<>();

        @NonNull
        private final ArrayList<Item> mFooters = new ArrayList<>();

        private boolean mHideFootersIfEmpty;
        private boolean mHideHeadersIfEmpty;

        ArrayHeaderFooterAdapter(@NonNull ListAdapter adapter,
                                 @NonNull List<Item> headers,
                                 @NonNull List<Item> footers,
                                 boolean hideHeadersIfEmpty,
                                 boolean hideFootersIfEmpty) {
            super(adapter);
            mHeaders.addAll(headers);
            mFooters.addAll(footers);
            mHideHeadersIfEmpty = hideHeadersIfEmpty;
            mHideFootersIfEmpty = hideFootersIfEmpty;
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

        @NonNull
        @Override
        protected View getFooterView(@NonNull LayoutInflater layoutInflater,
                                     @NonNull ViewGroup parent,
                                     int position) {
            return mFooters.get(position).get(layoutInflater, parent);
        }

        @Override
        protected int getFooterCount(boolean visibleOnly) {
            if (visibleOnly && mHideFootersIfEmpty && isUnderlyingAdapterEmpty()) {
                return 0;
            }
            return mFooters.size();
        }

        private boolean isUnderlyingAdapterEmpty() {
            return mAdapter.isEmpty();
        }
    }
}
