package com.nextfaze.poweradapters;

import lombok.NonNull;

import javax.annotation.Nullable;

public interface Metadata {
    int getInt(@NonNull String key, int defaultValue);

    String getString(@NonNull String key, @Nullable String defaultValue);

    // TODO: Other common data types.
}
