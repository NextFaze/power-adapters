package com.nextfaze.poweradapters.internal;

import android.support.annotation.NonNull;

public final class Preconditions {
    private Preconditions() {
        throw new AssertionError();
    }

    @NonNull
    public static <T> T checkNotNull(T o, @NonNull String varName) {
        if (o == null) {
            throw new NullPointerException(varName);
        }
        return o;
    }
}
