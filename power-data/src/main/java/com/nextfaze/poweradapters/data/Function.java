package com.nextfaze.poweradapters.data;

import lombok.NonNull;

public interface Function<F, T> {
    @NonNull
    T apply(@NonNull F f);
}
