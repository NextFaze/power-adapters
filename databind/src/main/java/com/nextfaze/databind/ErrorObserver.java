package com.nextfaze.databind;

import lombok.NonNull;

public interface ErrorObserver {
    void onError(@NonNull Throwable e);
}
