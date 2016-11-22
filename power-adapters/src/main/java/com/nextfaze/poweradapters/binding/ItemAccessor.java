package com.nextfaze.poweradapters.binding;

import lombok.NonNull;

interface ItemAccessor<T> {
    @NonNull
    T get(int position);
}
