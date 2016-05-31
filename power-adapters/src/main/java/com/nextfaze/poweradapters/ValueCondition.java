package com.nextfaze.poweradapters;

public final class ValueCondition extends AbstractCondition {

    private boolean mValue;

    public ValueCondition() {
    }

    public ValueCondition(boolean defaultValue) {
        mValue = defaultValue;
    }

    @Override
    public boolean eval() {
        return mValue;
    }

    public boolean get() {
        return mValue;
    }

    public void set(boolean value) {
        if (value != mValue) {
            mValue = value;
            notifyChanged();
        }
    }
}
