package com.nextfaze.poweradapters.support;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import com.nextfaze.poweradapters.PowerAdapter;

public final class SupportPowerAdapters {

    private SupportPowerAdapters() {
    }

    @CheckResult
    @NonNull
    public static PagerAdapter toPagerAdapter(@NonNull PowerAdapter powerAdapter) {
        return new ConverterPagerAdapter(powerAdapter);
    }
}
