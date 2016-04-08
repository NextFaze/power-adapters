package com.nextfaze.poweradapters.data;

import lombok.NonNull;

public interface ErrorObserver {
    void onError(@NonNull Throwable e);
}
