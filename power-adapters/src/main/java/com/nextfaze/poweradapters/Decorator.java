package com.nextfaze.poweradapters;

import lombok.NonNull;

public interface Decorator {
    @NonNull
    PowerAdapter decorate(@NonNull PowerAdapter adapter);
}
