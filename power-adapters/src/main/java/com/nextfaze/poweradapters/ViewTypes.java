package com.nextfaze.poweradapters;

import lombok.NonNull;

public final class ViewTypes {

    private ViewTypes() {
    }

    @NonNull
    public static ViewType create() {
        return new ViewType() {
        };
    }
}
