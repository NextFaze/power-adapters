package com.nextfaze.poweradapters.sample.files;

import android.support.annotation.NonNull;

import com.nextfaze.poweradapters.data.ArrayData;

import java.util.List;

@Deprecated
final class DirectoryData extends ArrayData<File> {

    @NonNull
    private final File mDir;

    DirectoryData(@NonNull File dir) {
        mDir = dir;
    }

    @NonNull
    @Override
    protected List<File> load() throws Throwable {
        return mDir.listFiles();
    }
}
