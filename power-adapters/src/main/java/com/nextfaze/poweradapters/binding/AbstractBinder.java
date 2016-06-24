package com.nextfaze.poweradapters.binding;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.ViewFactory;
import lombok.NonNull;

import static com.nextfaze.poweradapters.ViewFactories.asViewFactory;

public abstract class AbstractBinder<T, V extends View> implements Binder<T, V> {

    @NonNull
    private final ViewFactory mViewFactory;

    protected AbstractBinder(@LayoutRes int layoutResource) {
        this(asViewFactory(layoutResource));
    }

    protected AbstractBinder(@NonNull ViewFactory viewFactory) {
        mViewFactory = viewFactory;
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent) {
        return mViewFactory.create(parent);
    }

    @Override
    public boolean isEnabled(@NonNull T t, int position) {
        return true;
    }

    @Override
    public long getItemId(@NonNull T t, int position) {
        return PowerAdapter.NO_ID;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /** By default, returns {@code this}, implying this binder supports a single view type. */
    @NonNull
    @Override
    public Object getViewType(@NonNull T t, int position) {
        return this;
    }
}
