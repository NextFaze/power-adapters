package com.nextfaze.poweradapters.binding;

import android.support.annotation.NonNull;
import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;

public interface BindViewHolderFunction<T, H extends ViewHolder> {
    void bindViewHolder(@NonNull Container container, @NonNull T t, @NonNull H h, @NonNull Holder holder);
}
