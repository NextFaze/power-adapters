package com.nextfaze.poweradapters;

import android.support.annotation.UiThread;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;

class ItemAdapter extends AbstractPowerAdapter {

    @NonNull
    private final List<Item> mItems;

    /**
     * Indicates the visibility of each item. They're visible by default.
     * Index = adapter position
     * Key = item position
     */
    @NonNull
    private final SparseArray<Item> mVisibleItems = new SparseArray<>();

    ItemAdapter(@NonNull Item item) {
        mItems = singletonList(item);
        mVisibleItems.put(0, item);
    }

    ItemAdapter(@NonNull Collection<Item> items) {
        mItems = new ArrayList<>(items);
        for (int i = 0; i < mItems.size(); i++) {
            mVisibleItems.put(i, mItems.get(i));
        }
    }

    @Override
    public final int getItemCount() {
        return mVisibleItems.size();
    }

    @Override
    public final int getViewTypeCount() {
        return mItems.size();
    }

    @Override
    public final int getItemViewType(int position) {
        return mVisibleItems.keyAt(position);
    }

    @Override
    public final boolean isEnabled(int position) {
        return mVisibleItems.valueAt(position).isEnabled();
    }

    @NonNull
    @Override
    public final View newView(@NonNull ViewGroup parent, int itemViewType) {
        return mItems.get(itemViewType).create(parent);
    }

    @Override
    public final void bindView(@NonNull View view, @NonNull Holder holder) {
    }

    @UiThread
    final void setVisible(int index, boolean visible) {
        boolean wasVisible = mVisibleItems.get(index) != null;
        if (wasVisible != visible) {
            if (visible) {
                mVisibleItems.put(index, mItems.get(index));
                notifyItemInserted(index);
            } else {
                mVisibleItems.remove(index);
                notifyItemRemoved(index);
            }
        }
    }

    @UiThread
    final void setAllVisible(boolean visible) {
        for (int i = 0; i < mItems.size(); i++) {
            setVisible(i, visible);
        }
    }

    @UiThread
    final boolean isVisible(int index) {
        return mVisibleItems.get(index) != null;
    }
}
