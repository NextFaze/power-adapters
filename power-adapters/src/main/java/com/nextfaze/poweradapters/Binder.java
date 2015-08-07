package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

/**
 * Binds an object to a {@link View}. The binder is also responsible for creating the view, and determining if it is
 * enabled (which means it can be clicked within a list).
 */
public interface Binder {
    @NonNull
    View newView(@NonNull ViewGroup parent);

    void bindView(@NonNull Object obj, @NonNull View v, int position);

    boolean isEnabled(int position);
}
