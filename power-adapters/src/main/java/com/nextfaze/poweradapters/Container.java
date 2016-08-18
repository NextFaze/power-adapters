package com.nextfaze.poweradapters;

import android.view.ViewGroup;
import lombok.NonNull;

public interface Container {
    void scrollToStart(); // TODO: Ensure this only scroll to beginning of local dataset.

    void scrollToEnd(); // TODO: Ensure this only scroll to end of local dataset.

    void scrollToPosition(int position);

    int getItemCount();

    @NonNull
    ViewGroup getViewGroup();

    @NonNull
    Container getRootContainer();
}
