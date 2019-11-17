package com.nextfaze.poweradapters.binding;

import androidx.annotation.NonNull;

interface ItemAccessor<T> {
    @NonNull
    T get(int position);
}
