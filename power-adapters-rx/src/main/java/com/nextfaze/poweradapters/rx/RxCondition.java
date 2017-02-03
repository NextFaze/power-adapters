package com.nextfaze.poweradapters.rx;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.nextfaze.poweradapters.Condition;
import com.nextfaze.poweradapters.Observer;
import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;
import static com.nextfaze.poweradapters.rx.internal.Utils.assertUiThread;

public final class RxCondition {

    private RxCondition() {
        throw new AssertionError();
    }

    @CheckResult
    @NonNull
    public static Observable<Boolean> value(@NonNull final Condition condition) {
        checkNotNull(condition, "condition");
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                assertUiThread();
                subscriber.onNext(condition.eval());
                final Observer dataObserver = new Observer() {
                    @Override
                    public void onChanged() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(condition.eval());
                        }
                    }
                };
                condition.registerObserver(dataObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
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
