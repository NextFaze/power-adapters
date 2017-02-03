package com.nextfaze.poweradapters.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.nextfaze.poweradapters.PowerAdapter;

import java.util.Collection;

/**
 * Used to determine which {@link Binder} should be used to bind an item to a {@link View} in a {@link PowerAdapter}.
 * @param <T> The type of items for which this {@code Mapper} can supply binders.
 */
public interface Mapper<T> {
    /**
     * Given a position and item object, returns a binder that should be used to bind it to a {@link View}.
     * @param item The data object to be bound, never {@code null}.
     * @param position The 0-based position in the data set.
     * @return A binder which will be used to bind this object to a view, possibly {@code null} if this mapper doesn't
     * support the object.
     */
    @Nullable
    Binder<T, View> getBinder(@NonNull T item, int position);

    /** Return an immutable collection of the possible binders this mapper could respond with. */
    @NonNull
    Collection<? extends Binder<T, ?>> getAllBinders();

    /**
     * Returns whether the IDs supplied by all binders are considered stable.
     * @see PowerAdapter#hasStableIds()
     */
    boolean hasStableIds();
}
