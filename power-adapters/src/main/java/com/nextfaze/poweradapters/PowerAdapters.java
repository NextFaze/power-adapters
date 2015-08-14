package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.widget.ListAdapter;
import lombok.NonNull;

public final class PowerAdapters {

    private PowerAdapters() {
    }

    @CheckResult
    @NonNull
    public static ListAdapter toListAdapter(@NonNull PowerAdapter powerAdapter) {
        return new ListAdapterConverterAdapter(powerAdapter);
    }

    @CheckResult
    @NonNull
    public static PowerAdapter concat(@NonNull PowerAdapter... powerAdapters) {
        return new ConcatAdapter(powerAdapters);
    }

    @CheckResult
    @NonNull
    public static PowerAdapter concat(@NonNull Iterable<? extends PowerAdapter> powerAdapters) {
        return new ConcatAdapter(powerAdapters);
    }
}
