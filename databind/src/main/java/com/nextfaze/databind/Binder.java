package com.nextfaze.databind;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

/** Binds an object to a {@link View}. The binder is also responsible for creating the view. */
public interface Binder {
    @NonNull
    View newView(@NonNull ViewGroup parent);

    void bindView(@NonNull Object obj, @NonNull View v, int position);

    boolean isEnabled(int position);
}
