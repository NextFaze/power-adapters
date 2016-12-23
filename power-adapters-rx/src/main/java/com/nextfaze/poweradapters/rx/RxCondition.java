package com.nextfaze.poweradapters.rx;

import android.support.annotation.CheckResult;
import com.nextfaze.poweradapters.Condition;
import lombok.NonNull;
import rx.Observable;

public final class RxCondition {

    private RxCondition() {
        throw new AssertionError();
    }

    /**
     * Creates a {@link Condition} that derives is value from the latest value emitted by the specified {@link
     * Observable}. Errors sent by the observable are ignored, but it's recommended not to supply an observable that
     * is capable of errors or completion.
     */
    @CheckResult
    @NonNull
    public static Condition observableCondition(@NonNull Observable<Boolean> observable) {
        return new ObservableCondition(observable);
    }
}
