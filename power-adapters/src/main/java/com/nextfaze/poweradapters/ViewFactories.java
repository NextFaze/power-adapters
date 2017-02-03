package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

public final class ViewFactories {

    private ViewFactories() {
    }

    @CheckResult
    @NonNull
    public static ViewFactory asViewFactory(@LayoutRes final int layoutResource) {
        return new ViewFactory() {
            @NonNull
            @Override
            public View create(@NonNull ViewGroup parent) {
                return layoutInflater(parent).inflate(layoutResource, parent, false);
            }
        };
    }

    @CheckResult
    @NonNull
    public static ViewFactory asViewFactory(@LayoutRes final int layoutResource,
                                            @Nullable final View.OnClickListener onClickListener) {
        return new ViewFactory() {
            @NonNull
            @Override
            public View create(@NonNull ViewGroup parent) {
                View v = layoutInflater(parent).inflate(layoutResource, parent, false);
                v.setOnClickListener(onClickListener);
                return v;
            }
        };
    }

    @CheckResult
    @NonNull
    public static ViewFactory asViewFactory(@NonNull final LayoutInflater layoutInflater,
                                            @LayoutRes final int layoutResource,
                                            @Nullable final View.OnClickListener onClickListener) {
        return new ViewFactory() {
            @NonNull
            @Override
            public View create(@NonNull ViewGroup parent) {
                View v = layoutInflater.inflate(layoutResource, parent, false);
                v.setOnClickListener(onClickListener);
                return v;
            }
        };
    }
}
