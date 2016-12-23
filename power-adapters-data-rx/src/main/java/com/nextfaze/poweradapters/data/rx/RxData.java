package com.nextfaze.poweradapters.data.rx;

import android.support.annotation.CheckResult;
import com.nextfaze.poweradapters.Condition;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.SimpleDataObserver;
import com.nextfaze.poweradapters.data.AvailableObserver;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.ErrorObserver;
import com.nextfaze.poweradapters.data.LoadingObserver;
import com.nextfaze.poweradapters.rx.ChangeEvent;
import com.nextfaze.poweradapters.rx.InsertEvent;
import com.nextfaze.poweradapters.rx.MoveEvent;
import com.nextfaze.poweradapters.rx.RemoveEvent;
import lombok.NonNull;
import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;
import rx.functions.Func1;

import java.util.concurrent.Callable;

import static com.nextfaze.poweradapters.rx.internal.ThreadUtils.assertUiThread;
import static rx.Observable.fromCallable;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

public final class RxData {

    // TODO: Refactor to support back pressure.

    private RxData() {
    }

    @CheckResult
    @NonNull
    public static Observable<Integer> size(@NonNull Data<?> data) {
        return elements(data).map(new Func1<Data<?>, Integer>() {
            @Override
            public Integer call(Data<?> d) {
                return d.size();
            }
        }).distinctUntilChanged();
    }

    @CheckResult
    @NonNull
    public static <T> Observable<Data<T>> elements(@NonNull final Data<T> data) {
        return Observable.create(new Observable.OnSubscribe<Data<T>>() {
            @Override
            public void call(final Subscriber<? super Data<T>> subscriber) {
                assertUiThread();
                subscriber.onNext(data);
                final DataObserver dataObserver = new SimpleDataObserver() {
                    @Override
                    public void onChanged() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(data);
                        }
                    }
                };
                data.registerDataObserver(dataObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        data.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<ChangeEvent> changes(@NonNull final Data<?> data) {
        return Observable.create(new Observable.OnSubscribe<ChangeEvent>() {
            @Override
            public void call(final Subscriber<? super ChangeEvent> subscriber) {
                assertUiThread();
                final DataObserver dataObserver = new Observer() {
                    @Override
                    public void onItemRangeChanged(int positionStart, int itemCount) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(new ChangeEvent(positionStart, itemCount));
                        }
                    }
                };
                data.registerDataObserver(dataObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        data.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<InsertEvent> inserts(@NonNull final Data<?> data) {
        return Observable.create(new Observable.OnSubscribe<InsertEvent>() {
            @Override
            public void call(final Subscriber<? super InsertEvent> subscriber) {
                assertUiThread();
                final DataObserver dataObserver = new Observer() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(new InsertEvent(positionStart, itemCount));
                        }
                    }
                };
                data.registerDataObserver(dataObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        data.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<RemoveEvent> removes(@NonNull final Data<?> data) {
        return Observable.create(new Observable.OnSubscribe<RemoveEvent>() {
            @Override
            public void call(final Subscriber<? super RemoveEvent> subscriber) {
                assertUiThread();
                final DataObserver dataObserver = new Observer() {
                    @Override
                    public void onItemRangeRemoved(int positionStart, int itemCount) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(new RemoveEvent(positionStart, itemCount));
                        }
                    }
                };
                data.registerDataObserver(dataObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        data.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<MoveEvent> moves(@NonNull final Data<?> data) {
        return Observable.create(new Observable.OnSubscribe<MoveEvent>() {
            @Override
            public void call(final Subscriber<? super MoveEvent> subscriber) {
                assertUiThread();
                final DataObserver dataObserver = new Observer() {
                    @Override
                    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(new MoveEvent(fromPosition, toPosition, itemCount));
                        }
                    }
                };
                data.registerDataObserver(dataObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        data.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<Boolean> loading(@NonNull final Data<?> data) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                assertUiThread();
                subscriber.onNext(data.isLoading());
                final LoadingObserver loadingObserver = new LoadingObserver() {
                    @Override
                    public void onLoadingChange() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(data.isLoading());
                        }
                    }
                };
                data.registerLoadingObserver(loadingObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        data.unregisterLoadingObserver(loadingObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<Integer> available(@NonNull final Data<?> data) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                subscriber.onNext(data.available());
                final AvailableObserver availableObserver = new AvailableObserver() {
                    @Override
                    public void onAvailableChange() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(data.available());
                        }
                    }
                };
                data.registerAvailableObserver(availableObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        data.unregisterAvailableObserver(availableObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<Throwable> errors(@NonNull final Data<?> data) {
        return Observable.create(new Observable.OnSubscribe<Throwable>() {
            @Override
            public void call(final Subscriber<? super Throwable> subscriber) {
                final ErrorObserver errorObserver = new ErrorObserver() {
                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(e);
                        }
                    }
                };
                data.registerErrorObserver(errorObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        data.unregisterErrorObserver(errorObserver);
                    }
                });
            }
        });
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

    @NonNull
    static <T> Observable<T> mainThreadObservable(@NonNull Callable<T> callable) {
        return fromCallable(callable).subscribeOn(mainThread());
    }

    static class Observer extends SimpleDataObserver {
        @Override
        public void onChanged() {
        }
    }
}
