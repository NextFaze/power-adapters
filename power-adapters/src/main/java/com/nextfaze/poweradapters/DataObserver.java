package com.nextfaze.poweradapters;

public interface DataObserver {
    void onChanged();

    void onItemRangeChanged(int positionStart, int itemCount);

    void onItemRangeInserted(int positionStart, int itemCount);

    void onItemRangeRemoved(int positionStart, int itemCount);

    void onItemRangeMoved(int fromPosition, int toPosition, int itemCount);
}
