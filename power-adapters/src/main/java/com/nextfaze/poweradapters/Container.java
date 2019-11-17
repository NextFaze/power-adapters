package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

/**
 * Represents the abstract managing container of a single use of a {@link PowerAdapter}. This allows items in the
 * adapter to access the context in which they are bound.
 * <p>
 * Since adapters may be used by multiple clients at once, the container is only accessible at bind time.
 * @see PowerAdapter#bindView(Container, View, Holder, java.util.List)
 */
public abstract class Container {

    /** Scrolls to the first item in the container. */
    public final void scrollToStart() {
        scrollToPosition(0);
    }

    /** Scrolls to the last item in the container. */
    public final void scrollToEnd() {
        scrollToPosition(getItemCount() - 1);
    }

    /** Scrolls to the item with the specified position. */
    public abstract void scrollToPosition(int position);

    /** Returns the number of items in this container. */
    public abstract int getItemCount();

    /**
     * Returns the {@link ViewGroup} of this container. For example, this returns the {@code RecyclerView} instance, if
     * the {@link PowerAdapter} is being used by one.
     * @return The view group of this container.
     */
    @NonNull
    public abstract ViewGroup getViewGroup();

    /** Grants access to the root container, allowing scrolling to the absolute start or end. */
    @NonNull
    public abstract Container getRootContainer();
}
