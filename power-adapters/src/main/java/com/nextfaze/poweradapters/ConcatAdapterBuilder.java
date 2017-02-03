package com.nextfaze.poweradapters;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

final class ConcatAdapterBuilder {

    @NonNull
    private final ArrayList<PowerAdapter> mAdapters = new ArrayList<>();

    @NonNull
    ConcatAdapterBuilder add(@NonNull PowerAdapter adapter) {
        mAdapters.add(adapter);
        return this;
    }

    @NonNull
    ConcatAdapterBuilder addAll(@NonNull PowerAdapter... adapters) {
        Collections.addAll(mAdapters, adapters);
        return this;
    }

    @NonNull
    ConcatAdapterBuilder addAll(@NonNull Iterable<? extends PowerAdapter> adapters) {
        for (PowerAdapter adapter : adapters) {
            mAdapters.add(adapter);
        }
        return this;
    }

    @NonNull
    PowerAdapter build() {
        if (mAdapters.isEmpty()) {
            return PowerAdapter.EMPTY;
        }
        if (mAdapters.size() == 1) {
            return mAdapters.get(0);
        }
        return new ConcatAdapter(mAdapters);
    }
}
