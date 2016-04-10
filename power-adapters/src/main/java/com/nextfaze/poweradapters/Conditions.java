package com.nextfaze.poweradapters;

import lombok.NonNull;

public final class Conditions {

    private static final Condition ALWAYS = isTrue(true);
    private static final Condition NEVER = isTrue(false);

    private Conditions() {
    }

    @NonNull
    public static Condition isTrue(final boolean value) {
        return new AbstractCondition() {
            @Override
            public boolean eval() {
                return value;
            }
        };
    }

    @NonNull
    public static Condition isFalse(final boolean value) {
        return new AbstractCondition() {
            @Override
            public boolean eval() {
                return !value;
            }
        };
    }

    @NonNull
    public static Condition always() {
        return ALWAYS;
    }

    @NonNull
    public static Condition never() {
        return NEVER;
    }

    @NonNull
    public static Condition and(@NonNull final Condition a, @NonNull final Condition b) {
        return new CompoundCondition(a, b) {
            @Override
            public boolean eval() {
                return a.eval() && b.eval();
            }
        };
    }

    @NonNull
    public static Condition or(@NonNull final Condition a, @NonNull final Condition b) {
        return new CompoundCondition(a, b) {
            @Override
            public boolean eval() {
                return a.eval() || b.eval();
            }
        };
    }

    @NonNull
    public static Condition xor(@NonNull final Condition a, @NonNull final Condition b) {
        return new CompoundCondition(a, b) {
            @Override
            public boolean eval() {
                return (a.eval() || b.eval()) && !(a.eval() && b.eval());
            }
        };
    }

    @NonNull
    public static Condition not(@NonNull final Condition condition) {
        return new CompoundCondition(condition) {
            @Override
            public boolean eval() {
                return !condition.eval();
            }
        };
    }

    @NonNull
    public static Condition adapter(@NonNull PowerAdapter adapter,
                                    @NonNull Predicate<PowerAdapter> predicate) {
        return new AdapterCondition(adapter, predicate);
    }
}
