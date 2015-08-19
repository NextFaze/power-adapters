package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

final class Item implements ViewFactory {

    @NonNull
    private final ViewFactory mViewFactory;

    private final boolean mEnabled;

    Item(@LayoutRes int layoutResource) {
        this(layoutResource, true);
    }

    Item(@LayoutRes int layoutResource, boolean enabled) {
        this(ViewFactories.viewFactoryForResource(layoutResource), enabled);
    }

    Item(@NonNull View view) {
        this(view, true);
    }

    Item(@NonNull View view, boolean enabled) {
        this(ViewFactories.viewFactoryForView(view), enabled);
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
