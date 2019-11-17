package com.nextfaze.poweradapters.binding;

import android.view.View;

import androidx.annotation.NonNull;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

public class ViewHolder {

    @NonNull
    public final View view;

    public ViewHolder(@NonNull View view) {
        this.view = checkNotNull(view, "view");
    }
}
