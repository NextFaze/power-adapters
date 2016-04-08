package com.nextfaze.powerdata;

public interface DataObserver {
    void onChange();

    void onItemRangeChanged(int positionStart, int itemCount);

    void onItemRangeInserted(int positionStart, int itemCount);

    void onItemRangeRemoved(int positionStart, int itemCount);

    void onItemRangeMoved(int fromPosition, int toPosition, int itemCount);
}
