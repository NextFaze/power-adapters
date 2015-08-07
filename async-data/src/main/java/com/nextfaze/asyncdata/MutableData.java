package com.nextfaze.asyncdata;

import java.util.List;

public interface MutableData<T> extends Data<T>, List<T> {
    /** Marks the data as dirty, potentially triggering reloading. Does not clear the contents. */
    void invalidate();

    /** Clears the contents of the data, invalidating it in the process so loading begins again immediately. */
    @Override
    void clear();
}
