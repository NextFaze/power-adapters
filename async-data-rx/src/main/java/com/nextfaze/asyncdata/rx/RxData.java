package com.nextfaze.asyncdata.rx;

import android.support.annotation.CheckResult;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.DataObserver;
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
    public static <T> Observable<Change> changes(@NonNull final Data<T> data) {
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

    @NonNull
    private static <T> List<T> toList(@NonNull Data<T> data) {
        ArrayList<T> list = new ArrayList<T>(data.size());
        for (T t : data) {
            list.add(t);
        }
        return list;
    }
}
