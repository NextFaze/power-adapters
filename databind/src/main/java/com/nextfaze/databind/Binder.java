package com.nextfaze.databind;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

public interface Binder {
    @NonNull
    View newView(@NonNull ViewGroup parent);

    void bindView(@NonNull Object obj, @NonNull View v, int position);

    boolean isEnabled(int position);
}
