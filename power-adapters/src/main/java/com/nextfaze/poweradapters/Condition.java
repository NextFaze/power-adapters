package com.nextfaze.poweradapters;

import android.support.annotation.UiThread;
import lombok.NonNull;

import java.util.ArrayList;

public abstract class Condition {

    private static final Condition ALWAYS = isTrue(true);
    private static final Condition NEVER = isTrue(false);

    public abstract boolean eval();

    @NonNull
    private final ArrayList<Observer> mObservers = new ArrayList<>();

    /** Returns the number of registered observers. */
    protected final int getObserverCount() {
        return mObservers.size();
    }

    /** Called when the first observer has registered with this condition. */
    @UiThread
    protected void onFirstObserverRegistered() {
    }

    /** Called when the last observer has unregistered from this condition. */
    @UiThread
    protected void onLastObserverUnregistered() {
    }

    /** Notify observers that the condition has changed. */
    protected final void notifyChanged() {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onChanged();
        }
    }

    public void registerObserver(@NonNull Observer observer) {
        if (mObservers.contains(observer)) {
            throw new IllegalStateException("Observer is already registered.");
        }
        mObservers.add(observer);
        if (mObservers.size() == 1) {
            onFirstObserverRegistered();
        }
    }

    public void unregisterObserver(@NonNull Observer observer) {
        int index = mObservers.indexOf(observer);
        if (index == -1) {
            throw new IllegalStateException("Observer was not registered.");
        }
        mObservers.remove(index);
        if (mObservers.size() == 0) {
            onLastObserverUnregistered();
        }
    }

    @NonNull
    public Condition and(@NonNull Condition condition) {
        return and(this, condition);
    }

    @NonNull
    public Condition or(@NonNull Condition condition) {
        return or(this, condition);
    }

    @NonNull
    public Condition xor(@NonNull Condition condition) {
        return xor(this, condition);
    }

    @NonNull
    public Condition not() {
        return not(this);
    }

    @NonNull
    public static Condition isTrue(boolean value) {
        return new ConstantCondition(value);
    }

    @NonNull
    public static Condition isFalse(boolean value) {
        return new ConstantCondition(value);
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
        if (a instanceof ConstantCondition && b instanceof ConstantCondition) {
            return isTrue(a.eval() && b.eval());
        }
        return new CompoundCondition(a, b) {
            @Override
            public boolean eval() {
                return a.eval() && b.eval();
            }
        };
    }

    @NonNull
    public static Condition or(@NonNull final Condition a, @NonNull final Condition b) {
        if (a instanceof ConstantCondition && b instanceof ConstantCondition) {
            return isTrue(a.eval() || b.eval());
        }
        return new CompoundCondition(a, b) {
            @Override
            public boolean eval() {
                return a.eval() || b.eval();
            }
        };
    }

    @NonNull
    public static Condition xor(@NonNull final Condition a, @NonNull final Condition b) {
        if (a instanceof ConstantCondition && b instanceof ConstantCondition) {
            return isTrue((a.eval() || b.eval()) && !(a.eval() && b.eval()));
        }
        return new CompoundCondition(a, b) {
            @Override
            public boolean eval() {
                return (a.eval() || b.eval()) && !(a.eval() && b.eval());
            }
        };
    }

    @NonNull
    public static Condition not(@NonNull final Condition condition) {
        if (condition instanceof ConstantCondition) {
            return isTrue(!condition.eval());
        }
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
