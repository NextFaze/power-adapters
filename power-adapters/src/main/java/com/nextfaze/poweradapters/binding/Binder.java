package com.nextfaze.poweradapters.binding;

import android.view.View;
import android.view.ViewGroup;

import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.ViewFactory;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import static com.nextfaze.poweradapters.ViewFactories.asViewFactory;
import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

/** Binds an object to a {@link View} in a {@link PowerAdapter}. */
public abstract class Binder<T, V extends View> {

    @NonNull
    public static <T, V extends View> Binder<T, V> create(@LayoutRes int layoutResource,
                                                          @NonNull BindViewFunction<T, V> bindFunction) {
        return create(asViewFactory(layoutResource), bindFunction);
    }

    @NonNull
    public static <T, V extends View> Binder<T, V> create(@NonNull final ViewFactory viewFactory,
                                                          @NonNull final BindViewFunction<T, V> function) {
        checkNotNull(viewFactory, "viewFactory");
        checkNotNull(function, "function");
        return new Binder<T, V>() {
            @NonNull
            @Override
            public View newView(@NonNull ViewGroup parent) {
                return viewFactory.create(parent);
            }

            @Override
            public void bindView(
                    @NonNull Container container,
                    @NonNull T t,
                    @NonNull V v,
                    @NonNull Holder holder,
                    @NonNull List<Object> payloads
            ) {
                function.bindView(container, t, v, holder);
            }
        };
    }

    /**
     * Creates a {@link View} to be bound later by this binder instance. The view will be reused.
     * @param parent The destination parent view group of the view.
     * @return A new view capable of presenting the object that this binder expects later in its {@link
     * #bindView(Container, T, View, Holder, List)} method.
     * @see PowerAdapter#newView(ViewGroup, Object)
     */
    @NonNull
    public abstract View newView(@NonNull ViewGroup parent);

    /**
     * Bind the specified object to the specified {@link View}. The {@code View} is guaranteed to have been
     * instantiated by {@link #newView(ViewGroup)}.
     * @param container The {@link Container} that owns the view to be bound.
     * @param t The item object to be bound.
     * @param v The destination view.
     * @param holder A "holder" object which can be queried to determine the position of the item in the data set.
     * @param payloads A list of merged payload objects. Can be empty if a full update is required.
     * @see Holder
     * @see PowerAdapter#bindView(Container, View, Holder, java.util.List)
     */
    public abstract void bindView(
            @NonNull Container container,
            @NonNull T t,
            @NonNull V v,
            @NonNull Holder holder,
            @NonNull List<Object> payloads
    );

    /** @see PowerAdapter#isEnabled(int) */
    public boolean isEnabled(@NonNull T t, int position) {
        return true;
    }

    /** @see PowerAdapter#getItemId(int) */
    public long getItemId(@NonNull T t, int position) {
        return PowerAdapter.NO_ID;
    }

    /**
     * By default, returns {@code this}, implying this binder supports a single view type.
     * @param t The item object for which to return the view type.
     * @param position The position of the item object in the data set.
     * @return An object that is unique for this type of view for the lifetime of the owning adapter. By default, {@code
     * this}.
     * @see PowerAdapter#getItemViewType(int)
     */
    @NonNull
    public Object getViewType(@NonNull T t, int position) {
        return this;
    }

    /**
     * By default, returns {@code this}, implying this binder supports a single view type.
     * @see PowerAdapter#hasStableIds()
     */
    public boolean hasStableIds() {
        return false;
    }

    /** Inflate the specified layout resource. */
    @NonNull
    protected View inflate(@NonNull ViewGroup parent, @LayoutRes int layoutResource) {
        return layoutInflater(parent).inflate(layoutResource, parent, false);
    }
}
