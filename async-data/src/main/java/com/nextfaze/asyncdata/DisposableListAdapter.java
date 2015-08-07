package com.nextfaze.asyncdata;

import android.widget.ListAdapter;

public interface DisposableListAdapter extends ListAdapter {
    void dispose();
}
