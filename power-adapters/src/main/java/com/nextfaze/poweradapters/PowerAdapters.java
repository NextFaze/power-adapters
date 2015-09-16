package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.widget.ListAdapter;
import com.nextfaze.poweradapters.internal.WeakMap;
import lombok.NonNull;

import static java.util.Arrays.asList;

public final class PowerAdapters {

    private static final WeakMap<PowerAdapter, ListAdapterConverterAdapter> sListConverterAdapters = new WeakMap<>();

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
        if (powerAdapters.length == 1) {
            return powerAdapters[0];
        }
        return new ConcatAdapter(asList(powerAdapters));
    }

    @CheckResult
    @NonNull
    public static PowerAdapter concat(@NonNull Iterable<? extends PowerAdapter> powerAdapters) {
        return new ConcatAdapter(powerAdapters);
    }

}
