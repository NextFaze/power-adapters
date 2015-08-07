package com.nextfaze.poweradapters;

import android.view.LayoutInflater;
import android.view.View;
import lombok.NonNull;

final class AdapterUtils {
    AdapterUtils() {
    }

    @NonNull
    static LayoutInflater layoutInflater(@NonNull View v) {
        return LayoutInflater.from(v.getContext());
    }
}
