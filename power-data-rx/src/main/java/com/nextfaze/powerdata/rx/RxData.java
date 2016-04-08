package com.nextfaze.powerdata.rx;

import android.support.annotation.CheckResult;
import com.nextfaze.powerdata.AvailableObserver;
import com.nextfaze.powerdata.Data;
import com.nextfaze.powerdata.DataObserver;
import com.nextfaze.powerdata.ErrorObserver;
import com.nextfaze.powerdata.LoadingObserver;
import com.nextfaze.powerdata.SimpleDataObserver;
import lombok.NonNull;
import rx.Observable;
import rx.Subscriber;

import static com.nextfaze.powerdata.rx.ThreadUtils.assertUiThread;

public final class RxData {

    private RxData() {
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
                    public void onChange() {
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
    public static Observable<Change> changes(@NonNull final Data<?> data) {
        return Observable.create(new Observable.OnSubscribe<Change>() {
            @Override
            public void call(final Subscriber<? super Change> subscriber) {
                assertUiThread();
                final DataObserver dataObserver = new DataObserver() {
                    @Override
                    public void onChange() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(Change.newChange(0, data.size()));
                        }
                    }

                    @Override
                    public void onItemRangeChanged(int positionStart, int itemCount) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(Change.newChange(positionStart, itemCount));
                        }
                    }

                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(Change.newInsert(positionStart, itemCount));
                        }
                    }

                    @Override
                    public void onItemRangeRemoved(int positionStart, int itemCount) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(Change.newRemove(positionStart, itemCount));
                        }
                    }

                    @Override
                    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(Change.newMove(fromPosition, toPosition, itemCount));
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
}
