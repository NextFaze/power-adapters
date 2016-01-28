package com.nextfaze.poweradapters.recyclerview;

import android.support.annotation.CheckResult;
import android.support.v7.widget.RecyclerView;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.internal.WeakMap;
import lombok.NonNull;

public final class RecyclerPowerAdapters {

    private static final WeakMap<PowerAdapter, RecyclerConverterAdapter> sRecyclerConverterAdapters = new WeakMap<>();

    private RecyclerPowerAdapters() {
    }

    @CheckResult
    @NonNull
    public static RecyclerView.Adapter<?> toRecyclerAdapter(@NonNull PowerAdapter powerAdapter) {
        RecyclerConverterAdapter converterAdapter = sRecyclerConverterAdapters.get(powerAdapter);
        if (converterAdapter == null) {
            converterAdapter = new RecyclerConverterAdapter(powerAdapter);
            sRecyclerConverterAdapters.put(powerAdapter, converterAdapter);
        }
        return converterAdapter;
    }
}
