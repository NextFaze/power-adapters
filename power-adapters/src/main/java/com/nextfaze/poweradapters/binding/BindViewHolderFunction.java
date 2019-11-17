package com.nextfaze.poweradapters.binding;

import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;

import androidx.annotation.NonNull;

public interface BindViewHolderFunction<T, H extends ViewHolder> {
    void bindViewHolder(@NonNull Container container, @NonNull T t, @NonNull H h, @NonNull Holder holder);
}
