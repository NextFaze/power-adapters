package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

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
    public Object getViewType() {
        return mViewFactory;
    }

    @NonNull
    @Override
    public View create(@NonNull ViewGroup parent) {
        return mViewFactory.create(parent);
    }
}
