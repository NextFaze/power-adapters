package com.nextfaze.poweradapters.sample;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Collections.emptyList;

final class File {

    private static final int MAX_FILE_COUNT = 20;

    private final int mRandomSeed;

    @NonNull
    private final String mName;

    private final long mSize;

    private final int mFileCount;

    private final boolean mDir;

    @NonNull
    static File rootDir() {
        return new File(1, "/", 25, 0, true);
    }

    @NonNull
    static File createDir(@NonNull String name, int fileCount) {
        return new File(name.hashCode(), name, fileCount, 0, true);
    }

    @NonNull
    static File createFile(@NonNull String name) {
        int seed = name.hashCode();
        return new File(seed, name, 0, new Random(seed).nextInt(10), false);
    }

    private File(int randomSeed, @NonNull String name, int fileCount, long size, boolean dir) {
        mRandomSeed = randomSeed;
        mName = name;
        mFileCount = fileCount;
        mSize = size;
        mDir = dir;
    }

    int getCount() {
        return mFileCount;
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
        Random random = new Random(mRandomSeed);
        for (int i = 0; i < mFileCount; i++) {
            if (shouldBeDir(random)) {
                files.add(createDir(randomName(random), randomFileCount(random)));
            } else {
                files.add(createFile(randomName(random)));
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

    long getSize() {
        return mSize;
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
