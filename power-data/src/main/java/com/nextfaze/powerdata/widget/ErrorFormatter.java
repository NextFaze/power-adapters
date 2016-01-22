package com.nextfaze.powerdata.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import lombok.NonNull;

/** Converts a {@link Throwable} into a {@link CharSequence} for presentation in a {@link DataLayout}. */
public interface ErrorFormatter {
    @Nullable
    CharSequence format(@NonNull Context context, @NonNull Throwable e);

    /** Uses {@link Throwable#toString()} to render an error. */
    ErrorFormatter DEFAULT = new ErrorFormatter() {
        @Nullable
        @Override
        public String format(@NonNull Context context, @NonNull Throwable e) {
            return e.toString();
        }
    };
}
