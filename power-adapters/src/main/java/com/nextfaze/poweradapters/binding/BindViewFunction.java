package com.nextfaze.poweradapters.binding;

import android.view.View;

import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;

import androidx.annotation.NonNull;

public interface BindViewFunction<T, V extends View> {
    void bindView(@NonNull Container container, @NonNull T t, @NonNull V v, @NonNull Holder holder);
}
