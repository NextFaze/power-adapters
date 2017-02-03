package com.nextfaze.poweradapters.binding;

import android.support.annotation.NonNull;
import android.view.View;

public interface ViewHolderFactory<H extends ViewHolder> {
    @NonNull
    H create(@NonNull View v);
}
