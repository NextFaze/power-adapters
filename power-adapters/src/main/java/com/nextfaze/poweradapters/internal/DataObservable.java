package com.nextfaze.poweradapters.internal;

import android.support.annotation.RestrictTo;
import com.nextfaze.poweradapters.DataObserver;
import lombok.NonNull;

import java.util.ArrayList;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/** For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public final class DataObservable {

    @NonNull
    private final ArrayList<DataObserver> mObservers = new ArrayList<>();

    public void registerObserver(@NonNull DataObserver observer) {
        if (mObservers.contains(observer)) {
            throw new IllegalStateException("Observer is already registered.");
        }
        mObservers.add(observer);
    }

    public void unregisterObserver(@NonNull DataObserver observer) {
        int index = mObservers.indexOf(observer);
        if (index == -1) {
            throw new IllegalStateException("Observer was not registered.");
        }
        mObservers.remove(index);
    }

    public int getObserverCount() {
        return mObservers.size();
    }

    public void notifyDataSetChanged() {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onChanged();
        }
    }

    public void notifyItemChanged(int position) {
        notifyItemRangeChanged(position, 1);
    }

    public void notifyItemRangeChanged(int positionStart, int itemCount) {
        if (itemCount > 0) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeChanged(positionStart, itemCount);
            }
        }
    }

    public void notifyItemInserted(int position) {
        notifyItemRangeInserted(position, 1);
    }

    public void notifyItemRangeInserted(int positionStart, int itemCount) {
        if (itemCount > 0) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeInserted(positionStart, itemCount);
            }
        }
    }

    public void notifyItemMoved(int fromPosition, int toPosition) {
        notifyItemRangeMoved(fromPosition, toPosition, 1);
    }

    public void notifyItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        if (itemCount > 0) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeMoved(fromPosition, toPosition, itemCount);
            }
        }
    }

    public void notifyItemRemoved(int position) {
        notifyItemRangeRemoved(position, 1);
    }

    public void notifyItemRangeRemoved(int positionStart, int itemCount) {
        if (itemCount > 0) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeRemoved(positionStart, itemCount);
            }
        }
    }
}
