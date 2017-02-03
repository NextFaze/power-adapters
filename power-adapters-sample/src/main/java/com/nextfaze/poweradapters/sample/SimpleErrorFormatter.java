package com.nextfaze.poweradapters.sample;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.nextfaze.poweradapters.data.widget.ErrorFormatter;

final class SimpleErrorFormatter implements ErrorFormatter {
    @Nullable
    @Override
    public String format(@NonNull Context context, @NonNull Throwable e) {
        return "Failed to load: " + e.getMessage();
    }
}
