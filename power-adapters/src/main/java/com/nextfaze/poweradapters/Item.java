package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

final class Item implements ViewFactory {

    @NonNull
    private final ViewType mViewType = new ViewType();

    @NonNull
    private final ViewFactory mViewFactory;

    private final boolean mEnabled;

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
    ViewType getViewType() {
        return mViewType;
    }

    @NonNull
    @Override
    public View create(@NonNull ViewGroup parent) {
        return mViewFactory.create(parent);
    }
}
