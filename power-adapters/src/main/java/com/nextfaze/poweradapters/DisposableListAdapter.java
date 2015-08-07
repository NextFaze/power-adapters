package com.nextfaze.poweradapters;

import android.widget.ListAdapter;

public interface DisposableListAdapter extends ListAdapter {
    void dispose();
}
