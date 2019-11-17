package com.nextfaze.poweradapters.data;

import androidx.annotation.NonNull;

public interface ErrorObserver {
    void onError(@NonNull Throwable e);
}
