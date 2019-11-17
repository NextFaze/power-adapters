package com.nextfaze.poweradapters.rxjava2;

import androidx.annotation.NonNull;

public interface EqualityFunction<T> {
    boolean equal(@NonNull T a, @NonNull T b);
}
