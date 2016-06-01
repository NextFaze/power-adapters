package com.nextfaze.poweradapters.binding;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.ViewFactory;
import lombok.NonNull;

import static com.nextfaze.poweradapters.ViewFactories.asViewFactory;

/** A "type safe" binder implementation that performs the casts for you. */
public abstract class TypedBinder<T, V extends View> extends AbstractBinder<T, V> {

    @NonNull
    private final ViewFactory mViewFactory;

    private final boolean mEnabled;

    public TypedBinder(@LayoutRes int itemLayoutResource) {
        this(itemLayoutResource, true);
    }

    public TypedBinder(@LayoutRes int itemLayoutResource, boolean enabled) {
        this(asViewFactory(itemLayoutResource), enabled);
    }

    public TypedBinder(@NonNull ViewFactory viewFactory) {
        this(viewFactory, true);
    }

    public TypedBinder(@NonNull ViewFactory viewFactory, boolean enabled) {
        mViewFactory = viewFactory;
        mEnabled = enabled;
    }

    @NonNull
    @Override
    public final View newView(@NonNull ViewGroup parent) {
        return mViewFactory.create(parent);
    }

    @Override
    public void bindView(@NonNull T t, @NonNull V v, @NonNull Holder holder) {
        bind(t, v, holder);
    }

    @Override
    public boolean isEnabled(@NonNull T t, int position) {
        return mEnabled;
    }

    @Deprecated
    protected abstract void bind(@NonNull T t, @NonNull V v, @NonNull Holder holder);
}
