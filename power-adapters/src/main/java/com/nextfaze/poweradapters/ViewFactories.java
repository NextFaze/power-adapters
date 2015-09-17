package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import lombok.NonNull;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

public final class ViewFactories {

    private ViewFactories() {
    }

    @NonNull
    public static ViewFactory viewFactoryForResource(@LayoutRes final int layoutResource) {
        return new ViewFactory() {
            @NonNull
            @Override
            public View create(@NonNull ViewGroup parent) {
                return layoutInflater(parent).inflate(layoutResource, parent, false);
            }
        };
    }

    @NonNull
    public static ViewFactory viewFactoryForResource(@NonNull final LayoutInflater layoutInflater,
                                                     @LayoutRes final int layoutResource) {
        return new ViewFactory() {
            @NonNull
            @Override
            public View create(@NonNull ViewGroup parent) {
                return layoutInflater.inflate(layoutResource, parent, false);
            }
        };
    }

    /**
     * Warning: this is included for flexibility, but is not safe for use with a {@code RecyclerView}. In addition, the
     * view {@link LayoutParams} might not match those of parent collection view, leading to a crash.
     */
    @NonNull
    public static ViewFactory viewFactoryForView(@NonNull final View view) {
        return new ViewFactory() {
            @NonNull
            @Override
            public View create(@NonNull ViewGroup parent) {
                return view;
            }
        };
    }
}
