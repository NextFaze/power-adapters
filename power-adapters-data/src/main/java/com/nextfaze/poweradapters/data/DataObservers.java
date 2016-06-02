package com.nextfaze.poweradapters.data;

import com.nextfaze.poweradapters.DataObserver;

final class DataObservers extends Observers<DataObserver> {
    void notifyDataSetChanged() {
        for (DataObserver dataObserver : mObservers) {
            dataObserver.onChanged();
        }
    }

    void notifyItemRangeChanged(int positionStart, int itemCount) {
        for (DataObserver dataObserver : mObservers) {
            dataObserver.onItemRangeChanged(positionStart, itemCount);
        }
    }

    void notifyItemRangeInserted(int positionStart, int itemCount) {
        for (DataObserver dataObserver : mObservers) {
            dataObserver.onItemRangeInserted(positionStart, itemCount);
        }
    }

    void notifyItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        for (DataObserver dataObserver : mObservers) {
            dataObserver.onItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    }

    void notifyItemRangeRemoved(int positionStart, int itemCount) {
        for (DataObserver dataObserver : mObservers) {
            dataObserver.onItemRangeRemoved(positionStart, itemCount);
        }
    }
}
