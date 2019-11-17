package com.nextfaze.poweradapters;

import org.mockito.ArgumentMatcher;

import androidx.annotation.NonNull;

public final class ArgumentMatchers {
    private ArgumentMatchers() {
    }

    @NonNull
    public static ArgumentMatcher<Holder> holderWithPosition(final int expectedPosition) {
        return new ArgumentMatcher<Holder>() {
            @Override
            public boolean matches(Holder o) {
                Holder holder = (Holder) o;
                return holder.getPosition() == expectedPosition;
            }
        };
    }
}
