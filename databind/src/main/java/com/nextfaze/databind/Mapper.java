package com.nextfaze.databind;

import android.view.View;
import lombok.NonNull;

import java.util.Collection;

/** Used to determine which {@link Binder} should be used to bind a data item to a {@link View}. */
public interface Mapper {
    /**
     * Given a data item and position, returns the appropriate binder that should be used to bind it to a {@link View}.
     * @param item The data object to be bound.
     * @param position The position of the object in the data set.
     * @return A binder which will be used to bind this object to a view, never {@code null}.
     */
    @NonNull
    Binder getBinder(@NonNull Object item, int position);

    /** Return a collection containing all of the possible binders this mapper could respond with. */
    @NonNull
    Collection<? extends Binder> getAllBinders();
}
