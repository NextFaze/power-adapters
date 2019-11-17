package com.nextfaze.poweradapters.recyclerview;

import com.nextfaze.poweradapters.PowerAdapter;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

public final class RecyclerPowerAdapters {

    private RecyclerPowerAdapters() {
    }

    @CheckResult
    @NonNull
    public static RecyclerView.Adapter<?> toRecyclerAdapter(@NonNull PowerAdapter powerAdapter) {
        return new RecyclerConverterAdapter(checkNotNull(powerAdapter, "powerAdapter"));
    }
}
