package com.nextfaze.poweradapters;

import lombok.NonNull;

public class ContainerWrapper implements Container {

    @NonNull
    private final Container mContainer;

    public ContainerWrapper(@NonNull Container container) {
        mContainer = container;
    }

    @Override
    public void scrollToStart() {
        mContainer.scrollToStart();
    }

    @Override
    public void scrollToEnd() {
        mContainer.scrollToEnd();
    }

    @Override
    public void scrollToPosition(int position) {
        mContainer.scrollToPosition(position);
    }
}
