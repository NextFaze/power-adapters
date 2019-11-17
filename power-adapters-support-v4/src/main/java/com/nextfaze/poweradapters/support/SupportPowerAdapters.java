package com.nextfaze.poweradapters.support;

import com.nextfaze.poweradapters.PowerAdapter;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public final class SupportPowerAdapters {

    private SupportPowerAdapters() {
    }

    @CheckResult
    @NonNull
    public static PagerAdapter toPagerAdapter(@NonNull PowerAdapter powerAdapter) {
        return new ConverterPagerAdapter(powerAdapter);
    }
}
