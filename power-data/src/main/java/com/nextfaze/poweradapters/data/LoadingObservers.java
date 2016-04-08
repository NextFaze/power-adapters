package com.nextfaze.poweradapters.data;

final class LoadingObservers extends Observers<LoadingObserver> {
    void notifyLoadingChanged() {
        for (LoadingObserver loadingObserver : mObservers) {
            loadingObserver.onLoadingChange();
        }
    }
}
