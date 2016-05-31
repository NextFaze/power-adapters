package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

import static com.nextfaze.poweradapters.ViewFactories.asViewFactory;

final class Item implements ViewFactory {

    @NonNull
    private final ViewFactory mViewFactory;

    private final boolean mEnabled;

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

    Item(@NonNull ViewFactory viewFactory, boolean enabled) {
        mViewFactory = viewFactory;
        mEnabled = enabled;
    }

    boolean isEnabled() {
        return mEnabled;
    }

    @NonNull
    Item withEnabled(boolean enabled) {
        return mEnabled == enabled ? this : new Item(mViewFactory, enabled);
    }

    @NonNull
    @Override
    public View create(@NonNull ViewGroup parent) {
        return mViewFactory.create(parent);
    }
}
