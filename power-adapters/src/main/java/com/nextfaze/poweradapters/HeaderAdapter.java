package com.nextfaze.poweradapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

abstract class HeaderAdapter extends PowerAdapterWrapper {

    HeaderAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
    }

    @NonNull
    abstract View getHeaderView(@NonNull LayoutInflater layoutInflater,
                                @NonNull ViewGroup parent,
                                int headerIndex);

    abstract int getHeaderCount(boolean visibleOnly);

    @Override
    public final int getItemCount() {
        return getHeaderCount(true) + super.getItemCount();
    }

    @Override
    public final long getItemId(int position) {
        if (isHeader(position)) {
            return NO_ID;
        }
        return super.getItemId(position);
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
        return super.getItemViewType(position);
    }

    @Override
    public final boolean isEnabled(int position) {
        //noinspection SimplifiableIfStatement
        if (isHeader(position)) {
            return false;
        }
        return super.isEnabled(position);
    }

    @NonNull
    @Override
    public final View newView(@NonNull ViewGroup parent, int itemViewType) {
        int headerIndex = itemViewTypeToHeaderIndex(itemViewType);
        if (headerIndex != -1) {
            return getHeaderView(layoutInflater(parent), parent, headerIndex);
        }
        return super.newView(parent, itemViewType);
    }

    @Override
    public final void bindView(@NonNull View view, @NonNull Holder holder) {
        if (!isHeader(holder.getPosition())) {
            super.bindView(view, holder);
        }
    }

    @Override
    protected final int outerToInner(int outerPosition) {
        return outerPosition - getHeaderCount(true);
    }

    @Override
    protected int innerToOuter(int innerPosition) {
        return getHeaderCount(true) + innerPosition;
    }

    @Override
    public final boolean hasStableIds() {
        return super.hasStableIds();
    }

    private boolean isHeader(int position) {
        return position < getHeaderCount(true);
    }

    private int headerItemViewType(int position) {
        if (!isHeader(position)) {
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

    static final class Impl extends HeaderAdapter {

        @NonNull
        private final ArrayList<Item> mHeaders = new ArrayList<>();

        @NonNull
        private final HeaderAdapterBuilder.EmptyPolicy mEmptyPolicy;

        Impl(@NonNull PowerAdapter adapter, @NonNull List<Item> footers, @NonNull HeaderAdapterBuilder.EmptyPolicy emptyPolicy) {
            super(adapter);
            mEmptyPolicy = emptyPolicy;
            mHeaders.addAll(footers);
        }

        @NonNull
        @Override
        protected View getHeaderView(@NonNull LayoutInflater layoutInflater,
                                     @NonNull ViewGroup parent,
                                     int headerIndex) {
            return mHeaders.get(headerIndex).get(layoutInflater, parent);
        }

        @Override
        protected int getHeaderCount(boolean visibleOnly) {
            if (visibleOnly && !mEmptyPolicy.shouldShow(this)) {
                return 0;
            }
            return mHeaders.size();
        }
    }
}
