package com.nextfaze.poweradapters.binding;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.ViewFactory;
import com.nextfaze.poweradapters.ViewType;
import lombok.NonNull;

import static com.nextfaze.poweradapters.ViewFactories.viewFactoryForResource;

/** A "type safe" binder implementation that performs the casts for you. */
public abstract class TypedBinder<T, V extends View> implements Binder {

    @NonNull
    private final ViewType mViewType = new ViewType();

    @NonNull
    private final ViewFactory mViewFactory;

    private final boolean mEnabled;

    public TypedBinder(@LayoutRes int itemLayoutResource) {
        this(itemLayoutResource, true);
    }

    public TypedBinder(@LayoutRes int itemLayoutResource, boolean enabled) {
        this(viewFactoryForResource(itemLayoutResource), enabled);
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
        // Must return the view type specified by type argument.
        return mViewFactory.create(parent);
    }

    @Override
    public final void bindView(@NonNull Object obj, @NonNull View v, @NonNull Holder holder) {
        // Infrastructure ensures only the correct types are passed here.
        //noinspection unchecked
        bind((T) obj, (V) v, holder);
    }

    @NonNull
    @Override
    public final ViewType getViewType() {
        return mViewType;
    }

    @Override
    public boolean isEnabled(int position) {
        return mEnabled;
    }

    protected abstract void bind(@NonNull T t, @NonNull V v, @NonNull Holder holder);
}
