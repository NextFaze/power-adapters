package com.nextfaze.poweradapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

abstract class FooterAdapter extends PowerAdapterWrapper {

    FooterAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
    }

    @NonNull
    abstract View getFooterView(@NonNull LayoutInflater layoutInflater,
                                @NonNull ViewGroup parent,
                                int footerIndex);

    abstract int getFooterCount(boolean visibleOnly);

    @Override
    public final int getItemCount() {
        return getFooterCount(true) + super.getItemCount();
    }

    @Override
    public final long getItemId(int position) {
        if (isFooter(position)) {
            return NO_ID;
        }
        return super.getItemId(position);
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
        return super.getItemViewType(position);
    }

    @Override
    public final boolean isEnabled(int position) {
        //noinspection SimplifiableIfStatement
        if (isFooter(position)) {
            return false;
        }
        return super.isEnabled(position);
    }

    @NonNull
    @Override
    public final View newView(@NonNull ViewGroup parent, int itemViewType) {
        int footerIndex = itemViewTypeToFooterIndex(itemViewType);
        if (footerIndex != -1) {
            return getFooterView(layoutInflater(parent), parent, footerIndex);
        }
        return super.newView(parent, itemViewType);
    }

    @Override
    public final void bindView(@NonNull View view, @NonNull Holder holder) {
        if (!isFooter(holder.getPosition())) {
            super.bindView(view, holder);
        }
    }

    @Override
    protected final int outerToInner(int outerPosition) {
        // No conversion necessary, as footers appear at the end.
        return outerPosition;
    }

    @Override
    protected int innerToOuter(int innerPosition) {
        // No conversion necessary, as footers appear at the end.
        return super.innerToOuter(innerPosition);
    }

    @Override
    public final boolean hasStableIds() {
        return super.hasStableIds();
    }

    private boolean isFooter(int position) {
        return position >= super.getItemCount();
    }

    private int footerItemViewType(int position) {
        if (!isFooter(position)) {
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

}
