package com.nextfaze.asyncdata;

import lombok.NonNull;

public interface ErrorObserver {
    void onError(@NonNull Throwable e);
}
