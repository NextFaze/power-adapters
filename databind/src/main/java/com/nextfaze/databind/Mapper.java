package com.nextfaze.databind;

import lombok.NonNull;

public interface Mapper {
    @NonNull
    Binder getBinder(@NonNull Object item, int position);
}
