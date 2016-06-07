package com.nextfaze.poweradapters.sample;

import com.nextfaze.poweradapters.data.ArrayData;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

final class DirectoryData extends ArrayData<File> {

    private static final int FAKE_FILE_LIMIT = 5;

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
        Thread.sleep(200);
        return fakeDirContents();
    }

    @NonNull
    private List<File> fakeDirContents() {
        ArrayList<File> files = new ArrayList<>();
        for (int i = 0; i < FAKE_FILE_LIMIT && i < mLimit; i++) {
            if (shouldBeDir(i)) {
                files.add(new File("Dir #" + i, true));
            } else {
                files.add(new File("File #" + i, false));
            }
        }
        return files;
    }

    private static boolean shouldBeDir(int i) {
        return i == 2 || i == 3;
    }
}
