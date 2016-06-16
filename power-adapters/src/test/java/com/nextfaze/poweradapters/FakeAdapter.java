package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

public class FakeAdapter extends PowerAdapter {

    @NonNull
    private final ViewType mViewType = ViewTypes.create();

    private int mItemCount;

    @NonNull
    private NotificationType mNotificationType = NotificationType.FINE;

    public FakeAdapter(int itemCount) {
        mItemCount = itemCount;
    }

    @NonNull
    public NotificationType getNotificationType() {
        return mNotificationType;
    }

    public void setNotificationType(@NonNull NotificationType notificationType) {
        mNotificationType = notificationType;
    }

    @NonNull
    @Override
    public ViewType getItemViewType(int position) {
        assertWithinRange(position);
        return mViewType;
    }

    @Override
    public long getItemId(int position) {
        assertWithinRange(position);
        return NO_ID;
    }

    @Override
    public boolean isEnabled(int position) {
        assertWithinRange(position);
        return true;
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        return new View(parent.getContext());
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    public void insert(int positionStart, int itemCount) {
        mItemCount += itemCount;
        if (mNotificationType == NotificationType.COARSE) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    public void append(int itemCount) {
        insert(mItemCount, itemCount);
    }

    public void remove(int positionStart, int itemCount) {
        if (positionStart + itemCount > mItemCount) {
            throw new IndexOutOfBoundsException();
        }
        mItemCount -= itemCount;
        if (mNotificationType == NotificationType.COARSE) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(positionStart, itemCount);
        }
    }

    public void change(int positionStart, int itemCount) {
        if (mNotificationType == NotificationType.COARSE) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeChanged(positionStart, itemCount);
        }
    }

    public void move(int fromPosition, int toPosition, int itemCount) {
        if (mNotificationType == NotificationType.COARSE) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    }

    public void clear() {
        remove(0, mItemCount);
    }

    private void assertWithinRange(int position) {
        if (position >= mItemCount) {
            throw new IndexOutOfBoundsException();
        }
    }

    public enum NotificationType {
        COARSE, FINE
    }
}
