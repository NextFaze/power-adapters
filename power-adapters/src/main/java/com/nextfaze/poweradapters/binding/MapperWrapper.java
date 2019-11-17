package com.nextfaze.poweradapters.binding;

import android.view.View;

import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

public class MapperWrapper<T> implements Mapper<T> {

    @NonNull
    private final Mapper<T> mMapper;

    public MapperWrapper(@NonNull Mapper<T> mapper) {
        mMapper = checkNotNull(mapper, "mapper");
    }

    @NonNull
    public final Mapper<T> getMapper() {
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
