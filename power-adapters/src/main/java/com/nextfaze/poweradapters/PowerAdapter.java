package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import lombok.NonNull;

public interface PowerAdapter {

    int NO_ID = -1;

    /**
     * Returns the total number of items in the data set hold by the adapter.
     * @return The total number of items in this adapter.
     */
    int getItemCount();

    /**
     * Returns true if this adapter publishes a unique {@code long} value that can
     * act as a key for the item at a given position in the data set. If that item is relocated
     * in the data set, the ID returned for that item should be the same.
     * @return true if this adapter's items have stable IDs
     */
    boolean hasStableIds();

    /**
     * <p>
     * Returns the number of types of Views that will be created.
     * </p>
     * <p>
     * This method will only be called once when when the adapter is set on the
     * the {@link AdapterView}.
     * </p>
     * @return The number of types of Views that will be created by this adapter
     */
    int getViewTypeCount();

    /**
     * Return the stable ID for the item at {@code position}. If {@link #hasStableIds()}
     * would return false this method should return {@link #NO_ID}.
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    long getItemId(int position);

    /**
     * Get the type of View that will be created, for the purpose of view recycling.
     * @param position The position of the item within the adapter's data set whose view type we
     * want.
     * @return An integer representing the type of View. Two views should share the same type if one
     * can be converted to the other. Note: Integers must be in the range 0 to {@link #getViewTypeCount} - 1.
     */
    int getItemViewType(int position);

    /**
     * Returns metadata associated with this {@code position}.
     * @param position The position in the adapter's data set from which metadata will be retrieved.
     * @return Non-null metadata object containing additional information about this item.
     */
    @NonNull
    Metadata getItemMetadata(int position);

    @NonNull
    View newView(@NonNull ViewGroup parent, int itemViewType);

    void bindView(@NonNull View view, @NonNull Holder holder);

    void registerDataObserver(@NonNull DataObserver dataObserver);

    void unregisterDataObserver(@NonNull DataObserver dataObserver);
}
