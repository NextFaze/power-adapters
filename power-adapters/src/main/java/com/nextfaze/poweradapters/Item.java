package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

final class Item {

    @LayoutRes
    private final int mLayoutResource;

    @Nullable
    private final View mView;

    Item(int layoutResource) {
        mLayoutResource = layoutResource;
        mView = null;
    }

    @SuppressWarnings("NullableProblems")
    Item(@NonNull View view) {
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
}
