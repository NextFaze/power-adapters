package com.nextfaze.poweradapters.recyclerview;

import android.support.annotation.CheckResult;
import android.support.v7.widget.RecyclerView;
import com.nextfaze.poweradapters.PowerAdapter;
import lombok.NonNull;

import java.util.WeakHashMap;

public final class RecyclerPowerAdapters {

    private static final WeakHashMap<PowerAdapter, RecyclerConverterAdapter> sRecyclerConverterAdapters = new WeakHashMap<>();

    private RecyclerPowerAdapters() {
    }

    @CheckResult
    @NonNull
    public static RecyclerView.Adapter<?> toRecyclerAdapter(@NonNull PowerAdapter powerAdapter) {
        RecyclerConverterAdapter recyclerConverterAdapter = sRecyclerConverterAdapters.get(powerAdapter);
        if (recyclerConverterAdapter == null) {
            recyclerConverterAdapter = new RecyclerConverterAdapter(powerAdapter);
            sRecyclerConverterAdapters.put(powerAdapter, recyclerConverterAdapter);
        }
        return recyclerConverterAdapter;
    }
}
