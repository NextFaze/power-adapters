package com.nextfaze.poweradapters;

import lombok.NonNull;

public interface Condition {
    boolean eval();

    void registerObserver(@NonNull Observer observer);

    void unregisterObserver(@NonNull Observer observer);
}
