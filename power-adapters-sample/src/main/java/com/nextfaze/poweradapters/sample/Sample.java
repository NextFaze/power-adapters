package com.nextfaze.poweradapters.sample;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(prefix = "m")
public final class Sample implements Parcelable {

    public static final Creator<Sample> CREATOR = new Creator<Sample>() {
        @Override
        public Sample createFromParcel(Parcel parcel) {
            return new Sample(parcel);
        }

        @Override
        public Sample[] newArray(int size) {
            return new Sample[size];
        }
    };

    @NonNull
    private final String mName;

    @NonNull
    private final Class<? extends Fragment> mFragmentClass;

    @SuppressWarnings("unchecked")
    Sample(@NonNull Parcel parcel) {
        mName = parcel.readString();
        mFragmentClass = (Class<? extends Fragment>) parcel.readSerializable();
    }

    public Sample(@NonNull String name, @NonNull Class<? extends Fragment> fragmentClass) {
        mName = name;
        mFragmentClass = fragmentClass;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeSerializable(mFragmentClass);
    }
}
