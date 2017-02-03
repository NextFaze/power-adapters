package com.nextfaze.poweradapters.recyclerview;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import com.nextfaze.poweradapters.PowerAdapter;

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
