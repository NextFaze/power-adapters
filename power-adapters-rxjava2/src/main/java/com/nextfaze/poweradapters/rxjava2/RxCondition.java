package com.nextfaze.poweradapters.rxjava2;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.nextfaze.poweradapters.Condition;
import com.nextfaze.poweradapters.Observer;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.MainThreadDisposable;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;
import static io.reactivex.android.MainThreadDisposable.verifyMainThread;

public final class RxCondition {

    private RxCondition() {
        throw new AssertionError();
    }

    @CheckResult
    @NonNull
    public static Observable<Boolean> value(@NonNull final Condition condition) {
        checkNotNull(condition, "condition");
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> emitter) throws Exception {
                verifyMainThread();
                emitter.onNext(condition.eval());
                final Observer dataObserver = new Observer() {
                    @Override
                    public void onChanged() {
                        emitter.onNext(condition.eval());
                    }
                };
                condition.registerObserver(dataObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        condition.unregisterObserver(dataObserver);
                    }
                });
            }
        }).distinctUntilChanged();
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
