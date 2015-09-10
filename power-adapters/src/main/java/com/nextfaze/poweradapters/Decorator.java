package com.nextfaze.poweradapters;

import lombok.NonNull;

public interface Decorator {

    /** A decorator that simply returns the input {@link PowerAdapter}. */
    Decorator NULL = new Decorator() {
        @NonNull
        @Override
        public PowerAdapter decorate(@NonNull PowerAdapter adapter) {
            return adapter;
        }
    };

    @NonNull
    PowerAdapter decorate(@NonNull PowerAdapter adapter);
}
