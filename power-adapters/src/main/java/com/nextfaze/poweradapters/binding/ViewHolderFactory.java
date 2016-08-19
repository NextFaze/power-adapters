package com.nextfaze.poweradapters.binding;

import android.view.View;
import lombok.NonNull;

public interface ViewHolderFactory<H extends ViewHolder> {
    @NonNull
    H create(@NonNull View v);
}
