package com.nextfaze.poweradapters;

import android.database.Cursor;

import androidx.annotation.NonNull;

/** Converts a row of a {@link Cursor} to an object. */
public interface RowMapper<T> {
    @NonNull
    T map(@NonNull Cursor cursor);
}
