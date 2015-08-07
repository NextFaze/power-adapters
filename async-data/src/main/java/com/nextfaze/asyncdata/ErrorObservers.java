package com.nextfaze.asyncdata;

import lombok.NonNull;

final class ErrorObservers extends Observers<ErrorObserver> {
    void notifyError(@NonNull Throwable e) {
        for (ErrorObserver errorObserver : mObservers) {
            errorObserver.onError(e);
        }
    }
}
