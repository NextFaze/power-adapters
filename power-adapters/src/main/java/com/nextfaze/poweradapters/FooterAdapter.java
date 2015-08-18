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
    abstract View getFooterView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent, int index);

    abstract boolean isFooterEnabled(int index);

    abstract int getFooterCount(boolean visibleOnly);

    @Override
    public int getItemCount() {
        return getFooterCount(true) + super.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        if (isFooter(position)) {
            return NO_ID;
        }
        return super.getItemId(position);
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + getFooterCount(false);
    }

    @Override
    public int getItemViewType(int position) {
        int itemViewType = footerItemViewType(position);
        if (itemViewType != -1) {
            return itemViewType;
        }
        return super.getItemViewType(position);
    }

    @Override
    public boolean isEnabled(int position) {
        int index = footerIndex(position);
        if (index != -1) {
            return isFooterEnabled(index);
        }
        return super.isEnabled(position);
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
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        if (!isFooter(holder.getPosition())) {
            super.bindView(view, holder);
        }
    }

    @Override
    protected int outerToInner(int outerPosition) {
        // No conversion necessary, as footers appear at the end.
        return outerPosition;
    }

    @Override
    protected int innerToOuter(int innerPosition) {
        // No conversion necessary, as footers appear at the end.
        return super.innerToOuter(innerPosition);
    }

    private boolean isFooter(int position) {
        return footerIndex(position) != -1;
    }

    private int footerIndex(int position) {
        if (position < super.getItemCount()) {
            return -1;
        }
        return position - super.getItemCount();
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
