package com.nextfaze.poweradapters.internal;

import android.view.LayoutInflater;
import android.view.View;
import lombok.NonNull;

public final class AdapterUtils {
    private AdapterUtils() {
    }

    @NonNull
    public static LayoutInflater layoutInflater(@NonNull View v) {
        return LayoutInflater.from(v.getContext());
    }
}
