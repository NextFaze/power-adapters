package com.nextfaze.poweradapters;

import android.widget.ListAdapter;
import lombok.NonNull;

public final class PowerAdapters {

    private PowerAdapters() {
    }

    @NonNull
    public static ListAdapter toListAdapter(@NonNull PowerAdapter powerAdapter) {
        return new ListAdapterConverterAdapter(powerAdapter);
    }

    @NonNull
    public static PowerAdapter concat(@NonNull PowerAdapter... powerAdapters) {
        return new ConcatAdapter(powerAdapters);
    }

    @NonNull
    public static PowerAdapter concat(@NonNull Iterable<? extends PowerAdapter> powerAdapters) {
        return new ConcatAdapter(powerAdapters);
    }
}
