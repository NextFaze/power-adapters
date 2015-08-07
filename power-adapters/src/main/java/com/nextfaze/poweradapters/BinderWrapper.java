package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

public abstract class BinderWrapper implements Binder {

    @NonNull
    private final Binder mBinder;

    protected BinderWrapper(@NonNull Binder binder) {
        mBinder = binder;
    }

    @Override
    @NonNull
    public View newView(@NonNull ViewGroup viewGroup) {
        return mBinder.newView(viewGroup);
    }

    @Override
    public void bindView(@NonNull Object item, @NonNull View v, int position) {
        mBinder.bindView(item, v, position);
    }

    @Override
    public boolean isEnabled(int position) {
        return mBinder.isEnabled(position);
    }
}
