package com.nextfaze.poweradapters;

final class DataObservers extends Observers<DataObserver> {
    void notifyDataChanged() {
        for (DataObserver dataObserver : mObservers) {
            dataObserver.onChange();
        }
    }
}
