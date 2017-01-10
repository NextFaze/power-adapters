package com.nextfaze.poweradapters.internal;

import android.support.annotation.RestrictTo;
import android.view.LayoutInflater;
import android.view.View;
import lombok.NonNull;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

@RestrictTo(LIBRARY_GROUP)
public final class AdapterUtils {
    private AdapterUtils() {
    }

    @NonNull
    public static LayoutInflater layoutInflater(@NonNull View v) {
        return LayoutInflater.from(v.getContext());
    }
}
