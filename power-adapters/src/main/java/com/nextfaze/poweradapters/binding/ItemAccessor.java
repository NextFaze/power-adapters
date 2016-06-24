package com.nextfaze.poweradapters.binding;

import lombok.NonNull;

interface ItemAccessor {
    @NonNull
    Object get(int position);
}
