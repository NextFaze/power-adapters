package com.nextfaze.poweradapters.binding;

import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Holder;
import lombok.NonNull;

/**
 * Binds an object to a {@link View}. The binder is also responsible for creating the view, and determining if it is
 * enabled (which means it can be clicked within a list).
 */
public interface Binder {
    @NonNull
    View newView(@NonNull ViewGroup parent);

    /**
     * Bind the specified object to the specified {@link View}. The {@code View} is guaranteed to have been
     * instantiated by {@link #newView(ViewGroup)}.
     * @param obj The item object to be bound.
     * @param v The destination view.
     * @param holder A "holder" object which can be queried to determine the position of the item in the data set.
     * @see Holder
     */
    void bindView(@NonNull Object obj, @NonNull View v, @NonNull Holder holder);

    boolean isEnabled(int position);
}
