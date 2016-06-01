package com.nextfaze.poweradapters.binding;

import android.support.annotation.Nullable;
import lombok.NonNull;

import java.util.Collection;

public class MapperWrapper implements Mapper {

    @NonNull
    private final Mapper mMapper;

    public MapperWrapper(@NonNull Mapper mapper) {
        mMapper = mapper;
    }

    @NonNull
    public final Mapper getMapper() {
        return mMapper;
    }

    @Nullable
    @Override
    public Binder<?, ?> getBinder(@NonNull Object item, int position) {
        return mMapper.getBinder(item, position);
    }

    @NonNull
    @Override
    public Collection<? extends Binder<?, ?>> getAllBinders() {
        return mMapper.getAllBinders();
    }

    @Override
    public boolean hasStableIds() {
        return mMapper.hasStableIds();
    }
}
