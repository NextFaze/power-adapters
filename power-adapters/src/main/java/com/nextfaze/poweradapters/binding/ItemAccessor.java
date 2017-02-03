package com.nextfaze.poweradapters.binding;

import android.support.annotation.NonNull;

interface ItemAccessor<T> {
    @NonNull
    T get(int position);
}
