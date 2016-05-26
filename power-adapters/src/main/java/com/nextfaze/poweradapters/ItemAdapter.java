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

class ItemAdapter extends PowerAdapter {

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
            Item item = mItems.get(i);
            mVisibleItems.put(i, item);
        }
    }

    @Override
    public final int getItemCount() {
        return mVisibleItems.size();
    }

    @NonNull
    @Override
    public final ViewType getItemViewType(int position) {
        return mVisibleItems.valueAt(position);
    }

    @Override
    public final boolean isEnabled(int position) {
        return mVisibleItems.valueAt(position).isEnabled();
    }

    @NonNull
    @Override
    public final View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        Item item = (Item) viewType;
        // Note: cannot defensively remove view from parent first,
        // because AdapterView doesn't support removeView() in older versions.
        return item.create(parent);
    }

    @Override
    public final void bindView(@NonNull View view, @NonNull Holder holder) {
        // Nothing to bind. Each item represents a unique view.
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
