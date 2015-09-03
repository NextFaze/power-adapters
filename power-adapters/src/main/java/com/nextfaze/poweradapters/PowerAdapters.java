package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.widget.ListAdapter;
import lombok.NonNull;

import java.util.WeakHashMap;

public final class PowerAdapters {

    private static final WeakHashMap<PowerAdapter, ListAdapterConverterAdapter> sListConverterAdapters = new WeakHashMap<>();

    private PowerAdapters() {
    }

    @CheckResult
    @NonNull
    public static ListAdapter toListAdapter(@NonNull PowerAdapter powerAdapter) {
        ListAdapterConverterAdapter converterAdapter = sListConverterAdapters.get(powerAdapter);
        if (converterAdapter == null) {
            converterAdapter = new ListAdapterConverterAdapter(powerAdapter);
            sListConverterAdapters.put(powerAdapter, converterAdapter);
        }
        return converterAdapter;
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
