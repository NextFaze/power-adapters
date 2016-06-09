package com.nextfaze.poweradapters;

import lombok.NonNull;

final class DegenerateNotificationsAdapter extends PowerAdapterWrapper {

    DegenerateNotificationsAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
    }

    @Override
    protected void forwardChanged() {
        notifyDataSetChanged();
    }

    @Override
    protected void forwardItemRangeChanged(int innerPositionStart, int innerItemCount) {
        notifyDataSetChanged();
    }

    @Override
    protected void forwardItemRangeInserted(int innerPositionStart, int innerItemCount) {
        notifyDataSetChanged();
    }

    @Override
    protected void forwardItemRangeRemoved(int innerPositionStart, int innerItemCount) {
        notifyDataSetChanged();
    }

    @Override
    protected void forwardItemRangeMoved(int innerFromPosition, int innerToPosition, int innerItemCount) {
        notifyDataSetChanged();
    }
}
