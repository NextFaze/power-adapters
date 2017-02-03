package com.nextfaze.poweradapters.rx;

import android.support.annotation.NonNull;

public interface EqualityFunction<T> {
    boolean equal(@NonNull T a, @NonNull T b);
}
