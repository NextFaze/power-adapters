package com.nextfaze.poweradapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

public interface ViewFactory {
    @NonNull
    View create(@NonNull ViewGroup parent);
}
