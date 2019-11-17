package com.nextfaze.poweradapters.data;

import androidx.annotation.NonNull;

public interface Function<F, T> {
    @NonNull
    T apply(@NonNull F f);
}
