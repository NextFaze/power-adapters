package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

public final class ViewFactories {

    private ViewFactories() {
    }

    @Deprecated
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
    public static ViewFactory asViewFactory(@LayoutRes int layoutResource) {
        return viewFactoryForResource(layoutResource);
    }

    @Deprecated
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

    @NonNull
    public static ViewFactory asViewFactory(@NonNull LayoutInflater layoutInflater,
                                            @LayoutRes int layoutResource) {
        return viewFactoryForResource(layoutInflater, layoutResource);
    }
}
