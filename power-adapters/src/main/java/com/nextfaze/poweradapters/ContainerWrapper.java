package com.nextfaze.poweradapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

public class ContainerWrapper extends Container {

    @NonNull
    private final Container mContainer;

    public ContainerWrapper(@NonNull Container container) {
        mContainer = checkNotNull(container, "container");
    }

    @Override
    public void scrollToPosition(int position) {
        mContainer.scrollToPosition(position);
    }

    @Override
    public int getItemCount() {
        return mContainer.getItemCount();
    }

    @NonNull
    @Override
    public ViewGroup getViewGroup() {
        return mContainer.getViewGroup();
    }

    @NonNull
    @Override
    public Container getRootContainer() {
        return mContainer.getRootContainer();
    }
}
