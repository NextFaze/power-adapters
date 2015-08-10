package com.nextfaze.poweradapters;

import lombok.NonNull;

import javax.annotation.Nullable;

public interface Metadata {

    Metadata NONE = new Metadata() {
        @Override
        public int getInt(@NonNull String key, int defaultValue) {
            return defaultValue;
        }

        @Override
        public String getString(@NonNull String key, @Nullable String defaultValue) {
            return defaultValue;
        }
    };

    int getInt(@NonNull String key, int defaultValue);

    String getString(@NonNull String key, @Nullable String defaultValue);

    // TODO: Other common data types.
}
