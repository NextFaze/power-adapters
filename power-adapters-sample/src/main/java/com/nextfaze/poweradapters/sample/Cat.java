package com.nextfaze.poweradapters.sample;

import android.net.Uri;
import lombok.NonNull;

final class Cat {

    @NonNull
    private final String mName;

    @NonNull
    private final String mCountry;

    @NonNull
    private final Uri mImageUri;

    @NonNull
    static Cat create(@NonNull String name, @NonNull String country) {
        return new Cat(name, country, generateImageUri(name));
    }

    private Cat(@NonNull String name, @NonNull String country, @NonNull Uri imageUri) {
        mName = name;
        mCountry = country;
        mImageUri = imageUri;
    }

    @NonNull
    String getName() {
        return mName;
    }

    @NonNull
    String getCountry() {
        return mCountry;
    }

    @NonNull
    Uri getImageUri() {
        return mImageUri;
    }

    @NonNull
    private static Uri generateImageUri(@NonNull String str) {
        return Uri.parse("http://loremflickr.com/100/100/cat").buildUpon().query(str).build();
    }
}
