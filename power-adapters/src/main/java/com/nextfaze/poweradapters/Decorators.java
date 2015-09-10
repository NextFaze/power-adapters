package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import lombok.NonNull;

public class Decorators {

    private Decorators() {
    }

    @CheckResult
    @NonNull
    public static PowerAdapter decorate(@NonNull PowerAdapter adapter, @NonNull Decorator... decorators) {
        if (decorators.length == 0) {
            return adapter;
        }
        for (Decorator decorator : decorators) {
            adapter = decorator.decorate(adapter);
        }
        return adapter;
    }

    @CheckResult
    @NonNull
    public static PowerAdapter decorate(@NonNull PowerAdapter adapter,
                                        @NonNull Iterable<? extends Decorator> decorators) {
        for (Decorator decorator : decorators) {
            adapter = decorator.decorate(adapter);
        }
        return adapter;
    }
}
