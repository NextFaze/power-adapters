package com.nextfaze.asyncdata.rx;

import android.support.annotation.CheckResult;
import com.nextfaze.asyncdata.AvailableObserver;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.DataObserver;
import com.nextfaze.asyncdata.ErrorObserver;
import com.nextfaze.asyncdata.LoadingObserver;
import com.nextfaze.asyncdata.SimpleDataObserver;
import lombok.NonNull;
import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.List;

import static com.nextfaze.asyncdata.rx.ThreadUtils.assertUiThread;

public final class RxData {

    private RxData() {
    }

    @CheckResult
    @NonNull
    public static <T> Observable<List<T>> contents(@NonNull final Data<T> data) {
        // TODO: Copy vs non-copy version.
        return Observable.create(new Observable.OnSubscribe<List<T>>() {
            @Override
            public void call(final Subscriber<? super List<T>> subscriber) {
                assertUiThread();
                subscriber.onNext(toList(data));
                final DataObserver dataObserver = new SimpleDataObserver() {
                    @Override
                    public void onChange() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(toList(data));
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

    @NonNull
    private static <T> List<T> toList(@NonNull Data<T> data) {
        ArrayList<T> list = new ArrayList<T>(data.size());
        for (T t : data) {
            list.add(t);
        }
        return list;
    }
}
