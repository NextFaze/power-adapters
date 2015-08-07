package com.nextfaze.poweradapters;

import lombok.NonNull;

public interface ErrorObserver {
    void onError(@NonNull Throwable e);
}
