package com.nextfaze.poweradapters.sample;

import com.nextfaze.poweradapters.binding.BindingAdapter;
import com.nextfaze.poweradapters.binding.Mapper;
import lombok.NonNull;

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
