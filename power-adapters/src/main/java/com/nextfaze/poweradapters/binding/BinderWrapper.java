package com.nextfaze.poweradapters.binding;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;

import java.util.List;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

public abstract class BinderWrapper<T, V extends View> extends Binder<T, V> {

    @NonNull
    private final Binder<? super T, ? super V> mBinder;

    @NonNull
    static <T, V extends View> Binder<T, V> overrideLayout(@NonNull Binder<T, V> binder,
                                                           @LayoutRes final int layoutResource) {
        if (layoutResource <= 0) {
            return binder;
        }
        return new BinderWrapper<T, V>(binder) {
            @NonNull
            @Override
            public View newView(@NonNull ViewGroup viewGroup) {
                return layoutInflater(viewGroup).inflate(layoutResource, viewGroup, false);
            }
        };
    }

    protected BinderWrapper(@NonNull Binder<? super T, ? super V> binder) {
        mBinder = checkNotNull(binder, "binder");
    }

    @Override
    @NonNull
    public View newView(@NonNull ViewGroup viewGroup) {
        return mBinder.newView(viewGroup);
    }

    @Override
    public void bindView(
            @NonNull Container container,
            @NonNull T t,
            @NonNull V v,
            @NonNull Holder holder,
            @NonNull List<Object> payloads
    ) {
        mBinder.bindView(container, t, v, holder, payloads);
    }

    @Override
    public boolean isEnabled(@NonNull T t, int position) {
        return mBinder.isEnabled(t, position);
    }

    @Override
    public long getItemId(@NonNull T t, int position) {
        return mBinder.getItemId(t, position);
    }

    @NonNull
    @Override
    public Object getViewType(@NonNull T t, int position) {
        return mBinder.getViewType(t, position);
    }

    @Override
    public boolean hasStableIds() {
        return mBinder.hasStableIds();
    }
}
