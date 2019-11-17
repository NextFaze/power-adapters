package com.nextfaze.poweradapters;

import androidx.annotation.NonNull;

final class ConstantCondition extends Condition {

    private final boolean mValue;

    ConstantCondition(boolean value) {
        mValue = value;
    }

    @Override
    public boolean eval() {
        return mValue;
    }

    @Override
    public void registerObserver(@NonNull Observer observer) {
    }

    @Override
    public void unregisterObserver(@NonNull Observer observer) {
    }
}
