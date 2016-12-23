package com.nextfaze.poweradapters.rx;

import lombok.NonNull;

public interface EqualityFunction<T> {
    boolean equal(@NonNull T a, @NonNull T b);
}
