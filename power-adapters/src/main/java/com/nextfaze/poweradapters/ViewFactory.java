package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

public interface ViewFactory {
    @NonNull
    View create(@NonNull ViewGroup parent);
}
