package com.nextfaze.poweradapters.binding;

import java.util.Collection;
import java.util.List;

public abstract class AbstractMapper implements Mapper {

    /**
     * By default, if only a single {@link Binder} is present, returns {@link Binder#hasStableIds()}, otherwise returns
     * {@code false}
     */
    @Override
    public boolean hasStableIds() {
        Collection<? extends Binder<?, ?>> allBinders = getAllBinders();
        if (allBinders.size() == 1) {
            // Save an allocation by checking for List first.
            if (allBinders instanceof List) {
                //noinspection unchecked
                return ((List<Binder<?, ?>>) allBinders).get(0).hasStableIds();
            }
            return allBinders.iterator().next().hasStableIds();
        }
        // Can't possibly have stable IDs if we have multiple binders.
        return false;
    }
}
