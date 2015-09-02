package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

public interface ViewFactory {
    @NonNull
    View create(@NonNull ViewGroup parent);
}
