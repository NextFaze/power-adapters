package com.nextfaze.asyncdata;

final class AvailableObservers extends Observers<AvailableObserver> {
    void notifyAvailableChanged() {
        for (AvailableObserver availableObserver : mObservers) {
            availableObserver.onAvailableChange();
        }
    }
}
