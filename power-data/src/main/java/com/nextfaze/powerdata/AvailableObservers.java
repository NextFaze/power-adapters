package com.nextfaze.powerdata;

final class AvailableObservers extends Observers<AvailableObserver> {
    void notifyAvailableChanged() {
        for (AvailableObserver availableObserver : mObservers) {
            availableObserver.onAvailableChange();
        }
    }
}
