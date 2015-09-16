package com.nextfaze.poweradapters.binding;

import java.util.Collection;

public abstract class AbstractMapper implements Mapper {

    /**
     * By default, if only a single {@link Binder} is present, returns {@link Binder#hasStableIds()}, otherwise returns
     * {@code false}
     */
    @Override
    public boolean hasStableIds() {
        Collection<? extends Binder> allBinders = getAllBinders();
        //noinspection SimplifiableIfStatement
        if (allBinders.size() == 1) {
            return allBinders.iterator().next().hasStableIds();
        }
        return false;
    }
}
