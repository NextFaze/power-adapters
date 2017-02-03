package com.nextfaze.poweradapters.data;

import android.support.annotation.NonNull;

public interface Function<F, T> {
    @NonNull
    T apply(@NonNull F f);
}
