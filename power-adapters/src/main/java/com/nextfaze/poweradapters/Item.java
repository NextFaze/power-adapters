package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;

@Accessors(prefix = "m")
@AllArgsConstructor
final class Item {

    @LayoutRes
    private final int mLayoutResource;

    @Nullable
    private final View mView;

    @Wither
    private final boolean mEnabled;

    Item(int layoutResource) {
        this(layoutResource, true);
    }

    Item(int layoutResource, boolean enabled) {
        mLayoutResource = layoutResource;
        mEnabled = enabled;
        mView = null;
    }

    Item(@NonNull View view) {
        this(view, true);
    }

    Item(@SuppressWarnings("NullableProblems") @NonNull View view, boolean enabled) {
        mEnabled = enabled;
        mLayoutResource = 0;
        mView = view;
    }

    @NonNull
    View get(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        if (mLayoutResource > 0) {
            return layoutInflater.inflate(mLayoutResource, parent, false);
        }
        //noinspection ConstantConditions
        return mView;
    }

    boolean isEnabled() {
        return mEnabled;
    }
}
