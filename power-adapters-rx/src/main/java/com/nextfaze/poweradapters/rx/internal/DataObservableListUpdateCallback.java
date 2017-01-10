package com.nextfaze.poweradapters.rx.internal;

import android.support.annotation.RestrictTo;
import android.support.v7.util.ListUpdateCallback;
import com.nextfaze.poweradapters.internal.DataObservable;
import lombok.NonNull;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/** For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public final class DataObservableListUpdateCallback implements ListUpdateCallback {

    @NonNull
    private final DataObservable mDataObservable;

    public DataObservableListUpdateCallback(@NonNull DataObservable dataObservable) {
        mDataObservable = dataObservable;
    }

    @Override
    public void onInserted(int position, int count) {
        mDataObservable.notifyItemRangeInserted(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {
        mDataObservable.notifyItemRangeRemoved(position, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        mDataObservable.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onChanged(int position, int count, Object payload) {
        mDataObservable.notifyItemRangeChanged(position, count);
    }
}
