package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import static com.nextfaze.poweradapters.AdapterUtils.layoutInflater;

/** A "type safe" binder implementation that performs the casts for you. */
public abstract class TypedBinder<T, V extends View> implements Binder {

    @LayoutRes
    private final int mItemLayoutResource;

    private final boolean mEnabled;

    protected TypedBinder(@LayoutRes int itemLayoutResource) {
        this(itemLayoutResource, true);
    }

    protected TypedBinder(@LayoutRes int itemLayoutResource, boolean enabled) {
        mItemLayoutResource = itemLayoutResource;
        mEnabled = enabled;
    }

    @LayoutRes
    public final int getItemLayoutResource() {
        return mItemLayoutResource;
    }

    @NonNull
    @Override
    public final View newView(@NonNull ViewGroup parent) {
        // Must return the view type specified by type argument.
        return layoutInflater(parent).inflate(mItemLayoutResource, parent, false);
    }

    @Override
    public final void bindView(@NonNull Object obj, @NonNull View v, @NonNull Holder holder) {
        // Infrastructure ensures only the correct types are passed here.
        //noinspection unchecked
        bind((T) obj, (V) v, holder);
    }

    @Override
    public boolean isEnabled(int position) {
        return mEnabled;
    }

    protected abstract void bind(@NonNull T t, @NonNull V v, @NonNull Holder holder);
}
