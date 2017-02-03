package com.nextfaze.poweradapters.sample;

import android.support.annotation.NonNull;
import com.nextfaze.poweradapters.binding.BindingAdapter;
import com.nextfaze.poweradapters.binding.Mapper;

import java.io.File;
import java.util.List;

final class FileAdapter extends BindingAdapter {

    @NonNull
    private final List<File> mFiles;

    FileAdapter(@NonNull List<File> files, @NonNull Mapper mapper) {
        super(mapper);
        mFiles = files;
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    @NonNull
    @Override
    protected Object getItem(int position) {
        return mFiles.get(position);
    }
}
