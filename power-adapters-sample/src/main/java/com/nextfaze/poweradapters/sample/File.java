package com.nextfaze.poweradapters.sample;

import lombok.NonNull;

final class File {

    @NonNull
    private final String mName;

    private final boolean mDirectory;

    @NonNull
    static File rootFile() {
        return new File("/");
    }

    @NonNull
    static File fromRealFile(@NonNull java.io.File file) {
        return new File(file.getName(), file.isDirectory());
    }

    File(@NonNull String name) {
        this(name, true);
    }

    File(@NonNull String name, boolean directory) {
        mName = name;
        mDirectory = directory;
    }

    boolean isDirectory() {
        return mDirectory;
    }

    @NonNull
    String getName() {
        return mName;
    }
}
