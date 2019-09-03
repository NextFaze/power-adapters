package com.nextfaze.poweradapters;

import android.support.annotation.Nullable;

public interface DataObserver {
    void onChanged();

    void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload);

    void onItemRangeInserted(int positionStart, int itemCount);

    void onItemRangeRemoved(int positionStart, int itemCount);

    void onItemRangeMoved(int fromPosition, int toPosition, int itemCount);
}
