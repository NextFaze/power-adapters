package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import lombok.NonNull;

public class FakeAdapter extends PowerAdapter {

    @NonNull
    private final ViewType mViewType = ViewTypes.create();

    private int mItemCount;

    public FakeAdapter(int itemCount) {
        mItemCount = itemCount;
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
        // TODO: This isn't a stable ID. Not sure what else to return though.
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        assertWithinRange(position);
        return true;
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        return new FrameLayout(parent.getContext());
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    public void insert(int positionStart, int itemCount) {
        mItemCount += itemCount;
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void append(int itemCount) {
        insert(mItemCount, itemCount);
    }

    public void remove(int positionStart, int itemCount) {
        if (positionStart + itemCount > mItemCount) {
            throw new IndexOutOfBoundsException();
        }
        mItemCount -= itemCount;
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    public void change(int positionStart, int itemCount) {
        notifyItemRangeChanged(positionStart, itemCount);
    }

    public void move(int fromPosition, int toPosition, int itemCount) {
        notifyItemRangeMoved(fromPosition, toPosition, itemCount);
    }

    public void clear() {
        remove(0, mItemCount);
    }

    private void assertWithinRange(int position) {
        if (position >= mItemCount) {
            throw new IndexOutOfBoundsException();
        }
    }
}
