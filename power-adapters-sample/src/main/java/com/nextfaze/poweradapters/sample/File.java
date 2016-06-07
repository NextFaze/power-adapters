package com.nextfaze.poweradapters.sample;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Collections.emptyList;

final class File {

    private static final int MAX_FILE_COUNT = 20;

    @NonNull
    private final Random mRandom;

    @NonNull
    private final String mName;

    private final int mFileCount;

    private final boolean mDir;

    @NonNull
    static File rootFile() {
        return new File(new Random(1), "/", 5, true);
    }

    @NonNull
    static File createDir(@NonNull String name, int fileCount) {
        return new File(new Random(name.hashCode()), name, fileCount, true);
    }

    @NonNull
    static File createFile(@NonNull String name) {
        return new File(new Random(name.hashCode()), name, 0, false);
    }

    private File(@NonNull Random random, @NonNull String name, int fileCount, boolean dir) {
        mRandom = random;
        mName = name;
        mFileCount = fileCount;
        mDir = dir;
    }

    @NonNull
    List<File> listFiles() {
        if (!isDirectory()) {
            return emptyList();
        }
        try {
            // Fake sleep to pretend this is a slow disk I/O operation.
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ArrayList<File> files = new ArrayList<>();
        for (int i = 0; i < mFileCount; i++) {
            if (shouldBeDir(mRandom)) {
                files.add(createDir("Dir #" + randomName(mRandom), randomFileCount(mRandom)));
            } else {
                files.add(createFile("File #" + randomName(mRandom)));
            }
        }
        return files;
    }

    boolean isDirectory() {
        return mDir;
    }

    @NonNull
    String getName() {
        return mName;
    }

    private static boolean shouldBeDir(@NonNull Random random) {
        return random.nextBoolean();
    }

    @NonNull
    private static String randomName(@NonNull Random random) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            b.append(randomChar(random));
        }
        return b.toString();
    }

    private static char randomChar(@NonNull Random random) {
        return (char) ('a' + random.nextInt((int) 'z' - (int) 'a'));
    }

    private static int randomFileCount(@NonNull Random random) {
        if (random.nextInt(3) == 0) {
            return 0;
        }
        return 1 + random.nextInt(MAX_FILE_COUNT - 1);
    }
}
