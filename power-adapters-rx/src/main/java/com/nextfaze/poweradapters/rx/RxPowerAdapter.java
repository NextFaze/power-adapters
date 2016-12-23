package com.nextfaze.poweradapters.rx;

import android.support.annotation.CheckResult;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.SimpleDataObserver;
import lombok.NonNull;
import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

import static com.nextfaze.poweradapters.rx.internal.ThreadUtils.assertUiThread;

public final class RxPowerAdapter {

    private RxPowerAdapter() {
        throw new AssertionError();
    }

    @CheckResult
    @NonNull
    public static Observable<Integer> itemCount(@NonNull final PowerAdapter adapter) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                assertUiThread();
                final DataObserver dataObserver = new Observer() {
                    @Override
                    public void onChanged() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(adapter.getItemCount());
                        }
                    }
                };
                subscriber.onNext(adapter.getItemCount());
                adapter.registerDataObserver(dataObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        adapter.unregisterDataObserver(dataObserver);
                    }
                });
            }
        }).distinctUntilChanged();
    }

    @CheckResult
    @NonNull
    public static Observable<ChangeEvent> changes(@NonNull final PowerAdapter adapter) {
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
                adapter.registerDataObserver(dataObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        adapter.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<InsertEvent> inserts(@NonNull final PowerAdapter adapter) {
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
                adapter.registerDataObserver(dataObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        adapter.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<RemoveEvent> removes(@NonNull final PowerAdapter adapter) {
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
                adapter.registerDataObserver(dataObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        adapter.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<MoveEvent> moves(@NonNull final PowerAdapter adapter) {
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
                adapter.registerDataObserver(dataObserver);
                subscriber.add(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        adapter.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    static class Observer extends SimpleDataObserver {
        @Override
        public void onChanged() {
        }
    }
}
