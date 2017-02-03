package com.nextfaze.poweradapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.internal.NotificationType;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

@SuppressWarnings("WeakerAccess")
public class FakeAdapter extends PowerAdapter {

    private int mItemCount;

    @NonNull
    private NotificationType mNotificationType = NotificationType.FINE;

    public FakeAdapter(int itemCount) {
        mItemCount = itemCount;
    }

    @NonNull
    @Override
    public Object getItemViewType(int position) {
        assertWithinRange(position);
        return super.getItemViewType(position);
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
    public View newView(@NonNull ViewGroup parent, @NonNull Object viewType) {
        return new View(parent.getContext());
    }

    @Override
    public void bindView(@NonNull Container container, @NonNull View view, @NonNull Holder holder) {
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
        mNotificationType.notifyItemRangeInserted(mDataObservable, positionStart, itemCount);
    }

    public void append(int itemCount) {
        insert(mItemCount, itemCount);
    }

    public void remove(int positionStart, int itemCount) {
        if (positionStart + itemCount > mItemCount) {
            throw new IndexOutOfBoundsException();
        }
        mItemCount -= itemCount;
        mNotificationType.notifyItemRangeRemoved(mDataObservable, positionStart, itemCount);
    }

    public void change(int positionStart, int itemCount) {
        mNotificationType.notifyItemRangeChanged(mDataObservable, positionStart, itemCount);
    }

    public void move(int fromPosition, int toPosition, int itemCount) {
        mNotificationType.notifyItemRangeMoved(mDataObservable, fromPosition, toPosition, itemCount);
    }

    public void clear() {
        remove(0, mItemCount);
    }

    @SuppressWarnings("unused")
    @NonNull
    public NotificationType getNotificationType() {
        return mNotificationType;
    }

    public void setNotificationType(@NonNull NotificationType notificationType) {
        mNotificationType = checkNotNull(notificationType, "notificationType");
    }

    private void assertWithinRange(int position) {
        if (position >= mItemCount) {
            throw new IndexOutOfBoundsException();
        }
    }
}
