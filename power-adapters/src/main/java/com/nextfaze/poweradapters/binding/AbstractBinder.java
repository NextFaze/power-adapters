package com.nextfaze.poweradapters.binding;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.ViewFactory;

import static com.nextfaze.poweradapters.ViewFactories.asViewFactory;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

/** Use {@link Binder} and its factory methods instead. */
@Deprecated
public abstract class AbstractBinder<T, V extends View> extends Binder<T, V> {

    @NonNull
    private final ViewFactory mViewFactory;

    /** Use {@link Binder#create(int, BindViewFunction)} instead. */
    @Deprecated
    protected AbstractBinder(@LayoutRes int layoutResource) {
        this(asViewFactory(layoutResource));
    }

    protected AbstractBinder(@NonNull ViewFactory viewFactory) {
        mViewFactory = checkNotNull(viewFactory, "viewFactory");
    }

    @Override
    public void bindView(@NonNull Container container, @NonNull T t, @NonNull V v, @NonNull Holder holder) {
        //noinspection deprecation
        bindView(t, v, holder);
    }

    /** Use {@link #bindView(Container, T, View, Holder)} instead. */
    @Deprecated
    public void bindView(@NonNull T t, @NonNull V v, @NonNull Holder holder) {
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent) {
        return mViewFactory.create(parent);
    }
}
