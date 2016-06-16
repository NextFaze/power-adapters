package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

/** An adapter used for testing that has stable IDs. */
public class FakeLongAdapter extends ListAdapter<Long> {

    @NonNull
    private final ViewType mViewType = ViewTypes.create();

    @NonNull
    @Override
    public ViewType getItemViewType(int position) {
        return mViewType;
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
    public long getItemId(int position) {
        return get(position);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
