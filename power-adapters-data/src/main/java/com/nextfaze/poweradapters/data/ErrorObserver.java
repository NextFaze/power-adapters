package com.nextfaze.poweradapters.data;

import android.support.annotation.NonNull;

public interface ErrorObserver {
    void onError(@NonNull Throwable e);
}
