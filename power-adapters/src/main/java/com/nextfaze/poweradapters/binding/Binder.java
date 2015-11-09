package com.nextfaze.poweradapters.binding;

import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.ViewType;
import lombok.NonNull;

/**
 * Binds an object to a {@link View}. The binder is also responsible for creating the view, and determining if it is
 * enabled (which means it can be clicked within a list).
 */
public interface Binder {
    /**
     * Creates a {@link View} to be bound later by this binder instance. The view will be reused.
     * @param parent The destination parent view group of the view.
     * @return A new view capable of presenting the object that this binder expects later in its {@link
     * #bindView(Object, View, Holder)} method.
     * @see PowerAdapter#newView(ViewGroup, ViewType)
     */
    @NonNull
    View newView(@NonNull ViewGroup parent);

    /**
     * Bind the specified object to the specified {@link View}. The {@code View} is guaranteed to have been
     * instantiated by {@link #newView(ViewGroup)}.
     * @param obj The item object to be bound.
     * @param v The destination view.
     * @param holder A "holder" object which can be queried to determine the position of the item in the data set.
     * @see Holder
     * @see PowerAdapter#bindView(View, Holder)
     */
    void bindView(@NonNull Object obj, @NonNull View v, @NonNull Holder holder);

    /**
     * @see PowerAdapter#isEnabled(int)
     */
    boolean isEnabled(@NonNull Object obj, int position);

    /**
     * @see PowerAdapter#getItemId(int)
     */
    long getItemId(@NonNull Object obj, int position);

    @NonNull
    ViewType getViewType(@NonNull Object obj, int position);

    /**
     * @see PowerAdapter#hasStableIds()
     */
    boolean hasStableIds();
}
