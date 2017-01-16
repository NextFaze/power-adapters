package com.nextfaze.poweradapters.binding;

import android.support.annotation.Nullable;
import android.view.View;
import lombok.NonNull;

import java.util.Collection;

public class MapperWrapper<T> implements Mapper<T> {

    @NonNull
    private final Mapper<T> mMapper;

    public MapperWrapper(@NonNull Mapper<T> mapper) {
        mMapper = mapper;
    }

    @NonNull
    public final Mapper getMapper() {
        return mMapper;
    }

    @Nullable
    @Override
    public Binder<T, View> getBinder(@NonNull T item, int position) {
        return mMapper.getBinder(item, position);
    }

    @NonNull
    @Override
    public Collection<? extends Binder<T, ?>> getAllBinders() {
        return mMapper.getAllBinders();
    }

    @Override
    public boolean hasStableIds() {
        return mMapper.hasStableIds();
    }
}
