package com.nextfaze.poweradapters.internal;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

@RestrictTo(LIBRARY_GROUP)
public final class AdapterUtils {
    private AdapterUtils() {
    }

    @NonNull
    public static LayoutInflater layoutInflater(@NonNull View v) {
        return LayoutInflater.from(v.getContext());
    }
}
