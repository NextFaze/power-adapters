package com.nextfaze.poweradapters.sample;

import com.google.common.collect.FluentIterable;
import com.nextfaze.poweradapters.data.ArrayData;
import lombok.NonNull;

import java.io.File;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;

final class DirectoryData extends ArrayData<File> {

    @NonNull
    private final File mDir;

    private final int mLimit;

    DirectoryData(@NonNull File dir) {
        this(dir, Integer.MAX_VALUE);
    }

    DirectoryData(@NonNull File dir, int limit) {
        checkArgument(dir.isDirectory());
        mDir = dir;
        mLimit = limit;
    }

    @NonNull
    @Override
    protected List<? extends File> load() throws Throwable {
        // Fake sleep to better demonstrate async. loading.
        Thread.sleep(1000);
        File[] files = mDir.listFiles();
        if (files == null) {
            return emptyList();
        }
        return FluentIterable.of(files)
                .limit(mLimit)
                .toList();
    }
}
