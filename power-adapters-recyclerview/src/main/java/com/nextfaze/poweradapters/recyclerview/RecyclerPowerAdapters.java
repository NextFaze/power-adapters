package com.nextfaze.poweradapters.recyclerview;

import android.support.annotation.CheckResult;
import android.support.v7.widget.RecyclerView;
import com.nextfaze.poweradapters.PowerAdapter;
import lombok.NonNull;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public final class RecyclerPowerAdapters {

    private static final WeakHashMap<PowerAdapter, WeakReference<RecyclerConverterAdapter>> sRecyclerConverterAdapters = new WeakHashMap<>();

    private RecyclerPowerAdapters() {
    }

    @CheckResult
    @NonNull
    public static RecyclerView.Adapter<?> toRecyclerAdapter(@NonNull PowerAdapter powerAdapter) {
        WeakReference<RecyclerConverterAdapter> ref = sRecyclerConverterAdapters.get(powerAdapter);
        RecyclerConverterAdapter converterAdapter = ref != null ? ref.get() : null;
        if (converterAdapter == null) {
            converterAdapter = new RecyclerConverterAdapter(powerAdapter);
            ref = new WeakReference<>(converterAdapter);
            sRecyclerConverterAdapters.put(powerAdapter, ref);
            return converterAdapter;
        }
        return converterAdapter;
    }
}
