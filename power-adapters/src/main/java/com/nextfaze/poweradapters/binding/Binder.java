package com.nextfaze.poweradapters.binding;

import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import lombok.NonNull;

/**
 * Binds an object to a {@link View} in a {@link PowerAdapter}.
 */
public interface Binder<T, V extends View> {
    /**
     * Creates a {@link View} to be bound later by this binder instance. The view will be reused.
     * @param parent The destination parent view group of the view.
     * @return A new view capable of presenting the object that this binder expects later in its {@link
     * #bindView(Object, View, Holder)} method.
     * @see PowerAdapter#newView(ViewGroup, Object)
     */
    @NonNull
    View newView(@NonNull ViewGroup parent);

    /**
     * Bind the specified object to the specified {@link View}. The {@code View} is guaranteed to have been
     * instantiated by {@link #newView(ViewGroup)}.
     * @param t The item object to be bound.
     * @param v The destination view.
     * @param holder A "holder" object which can be queried to determine the position of the item in the data set.
     * @see Holder
     * @see PowerAdapter#bindView(Container, View, Holder)
     */
    void bindView(@NonNull T t, @NonNull V v, @NonNull Holder holder);

    /** @see PowerAdapter#isEnabled(int) */
    boolean isEnabled(@NonNull T t, int position);

    /** @see PowerAdapter#getItemId(int) */
    long getItemId(@NonNull T t, int position);

    /** @see PowerAdapter#getItemViewType(int) */
    @NonNull
    Object getViewType(@NonNull T t, int position);

    /** @see PowerAdapter#hasStableIds() */
    boolean hasStableIds();
}
