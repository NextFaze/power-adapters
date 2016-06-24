package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.widget.ListAdapter;
import com.nextfaze.poweradapters.internal.WeakMap;
import lombok.NonNull;

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
}
