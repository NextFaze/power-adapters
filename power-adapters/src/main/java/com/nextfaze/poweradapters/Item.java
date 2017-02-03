package com.nextfaze.poweradapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

final class Item implements ViewFactory {

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
    @Override
    public View create(@NonNull ViewGroup parent) {
        return mViewFactory.create(parent);
    }
}
