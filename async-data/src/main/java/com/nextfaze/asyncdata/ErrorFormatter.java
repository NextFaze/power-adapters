package com.nextfaze.asyncdata;

import android.content.Context;
import android.support.annotation.Nullable;
import lombok.NonNull;

public interface ErrorFormatter {
    @Nullable
    String format(@NonNull Context context, @NonNull Throwable e);

    /** Uses {@link Throwable#toString()} to render an error. */
    ErrorFormatter DEFAULT = new ErrorFormatter() {
        @Nullable
        @Override
        public String format(@NonNull Context context, @NonNull Throwable e) {
            return e.toString();
        }
    };
}
