package com.nextfaze.poweradapters.binding;

import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.ViewType;
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
    public void bindView(@NonNull Object item, @NonNull View v, @NonNull Holder holder) {
        mBinder.bindView(item, v, holder);
    }

    @Override
    public boolean isEnabled(int position) {
        return mBinder.isEnabled(position);
    }

    @Override
    public long getItemId(int position) {
        return mBinder.getItemId(position);
    }

    @NonNull
    @Override
    public ViewType getViewType() {
        return mBinder.getViewType();
    }

    @Override
    public boolean hasStableIds() {
        return mBinder.hasStableIds();
    }
}
