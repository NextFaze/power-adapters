package com.nextfaze.poweradapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.nextfaze.poweradapters.internal.WeakMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.nextfaze.poweradapters.ViewFactories.asViewFactory;

final class ItemAdapter extends PowerAdapter {

    @NonNull
    private final List<Item> mItems;

    @NonNull
    private final WeakMap<Object, Item> mViewTypeToItem = new WeakMap<>();

    @NonNull
    static List<Item> toItems(@NonNull Iterable<? extends ViewFactory> views) {
        ArrayList<Item> items = new ArrayList<>();
        for (ViewFactory v : views) {
            items.add(new Item(v, true));
        }
        return items;
    }

    @NonNull
    static List<Item> toItems(@NonNull int... resources) {
        ArrayList<Item> items = new ArrayList<>();
        for (Integer resource : resources) {
            items.add(new Item(asViewFactory(resource), true));
        }
        return items;
    }

    ItemAdapter(@NonNull Collection<? extends Item> items) {
        mItems = new ArrayList<>(items);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public Object getItemViewType(int position) {
        Item item = mItems.get(position);
        Object viewType = item.getViewType();
        mViewTypeToItem.put(viewType, item);
        return viewType;
    }

    @Override
    public boolean isEnabled(int position) {
        return mItems.get(position).isEnabled();
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull Object viewType) {
        Item item = mViewTypeToItem.get(viewType);
        if (item == null) {
            // Should never happen, as callers are expected to invoke getItemViewType(int) before invoking this method.
            throw new AssertionError("No item associated with view type");
        }
        // Note: cannot defensively remove view from parent first,
        // because AdapterView doesn't support removeView() in older versions.
        return item.create(parent);
    }

    @Override
    public void bindView(
            @NonNull Container container,
            @NonNull View view,
            @NonNull Holder holder,
            @NonNull List<Object> payloads
    ) {
        // Nothing to bind. Each item represents a unique view.
    }
}
