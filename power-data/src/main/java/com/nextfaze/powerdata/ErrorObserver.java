package com.nextfaze.powerdata;

import lombok.NonNull;

public interface ErrorObserver {
    void onError(@NonNull Throwable e);
}
