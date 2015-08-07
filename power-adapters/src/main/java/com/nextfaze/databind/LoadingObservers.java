package com.nextfaze.databind;

final class LoadingObservers extends Observers<LoadingObserver> {
    void notifyLoadingChanged() {
        for (LoadingObserver loadingObserver : mObservers) {
            loadingObserver.onLoadingChange();
        }
    }
}
