package com.nextfaze.poweradapters.binding;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.ViewFactory;
import lombok.NonNull;

import static com.nextfaze.poweradapters.ViewFactories.viewFactoryForResource;

/** A "type safe" binder implementation that performs the casts for you. */
public abstract class TypedBinder<T, V extends View> extends AbstractBinder {

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

    @Override
    public final boolean isEnabled(@NonNull Object obj, int position) {
        //noinspection unchecked
        return isEnabledChecked((T) obj, position);
    }

    @Override
    public final long getItemId(@NonNull Object obj, int position) {
        //noinspection unchecked
        return getItemIdChecked((T) obj, position);
    }

    protected long getItemIdChecked(@NonNull T t, int position) {
        return super.getItemId(t, position);
    }

    @SuppressWarnings("UnusedParameters")
    protected boolean isEnabledChecked(@NonNull T t, int position) {
        return mEnabled;
    }

    protected abstract void bind(@NonNull T t, @NonNull V v, @NonNull Holder holder);
}
