package com.nextfaze.poweradapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CheckResult;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
