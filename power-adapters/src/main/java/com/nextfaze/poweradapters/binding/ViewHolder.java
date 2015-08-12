package com.nextfaze.poweradapters.binding;

import android.view.View;
import lombok.NonNull;

public class ViewHolder {

    @NonNull
    public final View view;

    public ViewHolder(@NonNull View view) {
        this.view = view;
    }
}
