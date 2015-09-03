package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.widget.ListAdapter;
import lombok.NonNull;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import static java.util.Arrays.asList;

public final class PowerAdapters {

    private static final WeakHashMap<PowerAdapter, WeakReference<ListAdapterConverterAdapter>> sListConverterAdapters = new WeakHashMap<>();

    private PowerAdapters() {
    }

    @CheckResult
    @NonNull
    public static ListAdapter toListAdapter(@NonNull PowerAdapter powerAdapter) {
        WeakReference<ListAdapterConverterAdapter> ref = sListConverterAdapters.get(powerAdapter);
        ListAdapterConverterAdapter converterAdapter = ref != null ? ref.get() : null;
        if (converterAdapter == null) {
            converterAdapter = new ListAdapterConverterAdapter(powerAdapter);
            ref = new WeakReference<>(converterAdapter);
            sListConverterAdapters.put(powerAdapter, ref);
            return converterAdapter;
        }
        return converterAdapter;
    }

    @CheckResult
    @NonNull
    public static PowerAdapter concat(@NonNull PowerAdapter... powerAdapters) {
        return new ConcatAdapter(asList(powerAdapters));
    }

    @CheckResult
    @NonNull
    public static PowerAdapter concat(@NonNull Iterable<? extends PowerAdapter> powerAdapters) {
        return new ConcatAdapter(powerAdapters);
    }
}
