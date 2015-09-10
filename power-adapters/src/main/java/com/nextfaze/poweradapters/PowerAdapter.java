package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

public interface PowerAdapter {

    /** An adapter with no elements. */
    PowerAdapter EMPTY = new PowerAdapter() {
        @Override
        public int getItemCount() {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public ViewType getItemViewType(int position) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEnabled(int position) {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void bindView(@NonNull View view, @NonNull Holder holder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void registerDataObserver(@NonNull DataObserver dataObserver) {
        }

        @Override
        public void unregisterDataObserver(@NonNull DataObserver dataObserver) {
        }

        @NonNull
        @Override
        public PowerAdapter decorate(@NonNull Decorator decorator) {
            return decorator.decorate(this);
        }
    };

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
     * Return the stable ID for the item at {@code position}. If {@link #hasStableIds()}
     * would return false this method should return {@link #NO_ID}.
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    long getItemId(int position);

    /**
     * Get the type of view for item at {@code position}, for the purpose of view recycling.
     * @param position The position of the item within the adapter's data set whose view type we
     * want.
     * @return A {@link ViewType} object that is unique for this type of view throughout the process.
     */
    @NonNull
    ViewType getItemViewType(int position);

    /**
     * Returns true if the item at the specified position is not a separator.
     * (A separator is a non-selectable, non-clickable item).
     * The result is unspecified if position is invalid. An {@link ArrayIndexOutOfBoundsException}
     * should be thrown in that case for fast failure.
     * @param position Index of the item
     * @return True if the item is not a separator
     */
    boolean isEnabled(int position);

    @NonNull
    View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType);

    void bindView(@NonNull View view, @NonNull Holder holder);

    void registerDataObserver(@NonNull DataObserver dataObserver);

    void unregisterDataObserver(@NonNull DataObserver dataObserver);

    /** Wraps this adapter using the specified decorator, and returns the result. */
    @CheckResult
    @NonNull
    PowerAdapter decorate(@NonNull Decorator decorator);
}
