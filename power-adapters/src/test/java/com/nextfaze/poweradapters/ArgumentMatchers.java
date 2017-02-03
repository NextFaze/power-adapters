package com.nextfaze.poweradapters;

import android.support.annotation.NonNull;
import org.mockito.ArgumentMatcher;

public final class ArgumentMatchers {
    private ArgumentMatchers() {
    }

    @NonNull
    public static ArgumentMatcher<Holder> holderWithPosition(final int expectedPosition) {
        return new ArgumentMatcher<Holder>() {
            @Override
            public boolean matches(Object o) {
                Holder holder = (Holder) o;
                return holder.getPosition() == expectedPosition;
            }
        };
    }
}
