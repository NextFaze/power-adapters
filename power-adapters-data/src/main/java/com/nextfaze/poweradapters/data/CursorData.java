package com.nextfaze.poweradapters.data;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.io.Closeable;

/**
 * Simple {@link Data} implementation backed by a {@link Cursor}. The cursor can be changed or replaced at any time.
 * The cursor will never be closed.
 *
 * @param <T> The type of element this data contains.
 */
public abstract class CursorData<T> extends Data<T> implements Closeable {

    @NonNull
    private Cursor mData;

    public CursorData() {
    }

    /**
     * @return A new element without any data from the cursor.
     */
    public abstract T newElement();

    /**
     * @param element The element whose data needs to be updated with the information in the cursor.
     * @param cursor The cursor containing the information, currently at the position containing the element.
     */
    public abstract void populateElement(T element, Cursor cursor);

    /**
     * Replace the current cursor (if any) with the provided cursor.
     * @param data The new cursor. If null, the data will be empty.
     */
    public void setData(@NonNull Cursor data) {
        mData = data;
        invalidate();
    }

    @Override
    public final int size() {
        if (mData != null) {
            return mData.getCount();
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public final T get(int position, int flags) {
        T element = newElement();
        mData.moveToPosition(position);
        populateElement(element, mData);

        return element;
    }

    @Override
    public final void refresh() {
        throw new UnsupportedOperationException("Can't refresh a cursor. To load changes from the current cursor," +
                " call invalidate(). To supply a new cursor, call setCursor(cursor).");
    }

    @Override
    public final void reload() {
        throw new UnsupportedOperationException("Can't reload a cursor. To load changes from the current cursor," +
                " call invalidate(). To supply a new cursor, call setCursor(cursor).");
    }

    /**
     * Signal that a change has been made to the cursor, so views will need to bind again.
     */
    @Override
    public final void invalidate() {
        notifyDataSetChanged();
    }

    @Override
    public final boolean isLoading() {
        return false;
    }

    @Override
    public final int available() {
        return 0;
    }

}
