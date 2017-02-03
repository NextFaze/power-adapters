package com.nextfaze.poweradapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/** An adapter used for testing that has stable IDs. */
public class FakeLongAdapter extends ListAdapter<Long> {
    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull Object viewType) {
        return new View(parent.getContext());
    }

    @Override
    public void bindView(@NonNull Container container, @NonNull View view, @NonNull Holder holder) {
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
