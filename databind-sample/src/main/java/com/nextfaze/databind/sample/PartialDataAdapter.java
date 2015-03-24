package com.nextfaze.databind.sample;

import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.databind.Data;
import com.nextfaze.databind.DataAdapter;
import lombok.NonNull;

/** Bridge class that enables the use of a {@link DataAdapter} as the root of a chain of wrapped list adapters. */
public class PartialDataAdapter<T> extends DataAdapter<T> {
    public PartialDataAdapter(@NonNull Data<T> data) {
        super(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        throw new UnsupportedOperationException();
    }
}
