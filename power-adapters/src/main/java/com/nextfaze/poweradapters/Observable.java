package com.nextfaze.poweradapters;

final class Observable extends android.database.Observable<Observer> {

    final int size() {
        return mObservers.size();
    }

    final void notifyChanged() {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onChanged();
        }
    }
}
