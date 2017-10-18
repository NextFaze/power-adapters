package com.nextfaze.poweradapters.data.rxjava2;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.SimpleDataObserver;
import com.nextfaze.poweradapters.data.AvailableObserver;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.ErrorObserver;
import com.nextfaze.poweradapters.data.LoadingObserver;
import com.nextfaze.poweradapters.rxjava2.ChangeEvent;
import com.nextfaze.poweradapters.rxjava2.InsertEvent;
import com.nextfaze.poweradapters.rxjava2.MoveEvent;
import com.nextfaze.poweradapters.rxjava2.RemoveEvent;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.MainThreadDisposable;
import io.reactivex.functions.Function;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;
import static io.reactivex.android.MainThreadDisposable.verifyMainThread;

public final class RxData {

    private RxData() {
    }

    @CheckResult
    @NonNull
    public static Observable<Integer> size(@NonNull Data<?> data) {
        return elements(data).map(new Function<Data<?>, Integer>() {
            @Override
            public Integer apply(Data<?> d) {
                return d.size();
            }
        }).distinctUntilChanged();
    }

    @CheckResult
    @NonNull
    public static <T> Observable<Data<T>> elements(@NonNull final Data<T> data) {
        checkNotNull(data, "data");
        return Observable.create(new ObservableOnSubscribe<Data<T>>() {
            @Override
            public void subscribe(final ObservableEmitter<Data<T>> emitter) throws Exception {
                verifyMainThread();
                emitter.onNext(data);
                final DataObserver dataObserver = new SimpleDataObserver() {
                    @Override
                    public void onChanged() {
                        emitter.onNext(data);
                    }
                };
                data.registerDataObserver(dataObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        data.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<ChangeEvent> changes(@NonNull final Data<?> data) {
        checkNotNull(data, "data");
        return Observable.create(new ObservableOnSubscribe<ChangeEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<ChangeEvent> emitter) throws Exception {
                verifyMainThread();
                final DataObserver dataObserver = new Observer() {
                    @Override
                    public void onItemRangeChanged(int positionStart, int itemCount) {
                        emitter.onNext(new ChangeEvent(positionStart, itemCount));
                    }
                };
                data.registerDataObserver(dataObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        data.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<InsertEvent> inserts(@NonNull final Data<?> data) {
        checkNotNull(data, "data");
        return Observable.create(new ObservableOnSubscribe<InsertEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<InsertEvent> emitter) throws Exception {
                verifyMainThread();
                final DataObserver dataObserver = new Observer() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        emitter.onNext(new InsertEvent(positionStart, itemCount));
                    }
                };
                data.registerDataObserver(dataObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        data.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<RemoveEvent> removes(@NonNull final Data<?> data) {
        checkNotNull(data, "data");
        return Observable.create(new ObservableOnSubscribe<RemoveEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<RemoveEvent> emitter) throws Exception {
                verifyMainThread();
                final DataObserver dataObserver = new Observer() {
                    @Override
                    public void onItemRangeRemoved(int positionStart, int itemCount) {
                        emitter.onNext(new RemoveEvent(positionStart, itemCount));
                    }
                };
                data.registerDataObserver(dataObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        data.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<MoveEvent> moves(@NonNull final Data<?> data) {
        checkNotNull(data, "data");
        return Observable.create(new ObservableOnSubscribe<MoveEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<MoveEvent> emitter) throws Exception {
                verifyMainThread();
                final DataObserver dataObserver = new Observer() {
                    @Override
                    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                        emitter.onNext(new MoveEvent(fromPosition, toPosition, itemCount));
                    }
                };
                data.registerDataObserver(dataObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        data.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<Boolean> loading(@NonNull final Data<?> data) {
        checkNotNull(data, "data");
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> emitter) throws Exception {
                verifyMainThread();
                emitter.onNext(data.isLoading());
                final LoadingObserver loadingObserver = new LoadingObserver() {
                    @Override
                    public void onLoadingChange() {
                        emitter.onNext(data.isLoading());
                    }
                };
                data.registerLoadingObserver(loadingObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        data.unregisterLoadingObserver(loadingObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<Integer> available(@NonNull final Data<?> data) {
        checkNotNull(data, "data");
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(final ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(data.available());
                final AvailableObserver availableObserver = new AvailableObserver() {
                    @Override
                    public void onAvailableChange() {
                        emitter.onNext(data.available());
                    }
                };
                data.registerAvailableObserver(availableObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        data.unregisterAvailableObserver(availableObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<Throwable> errors(@NonNull final Data<?> data) {
        checkNotNull(data, "data");
        return Observable.create(new ObservableOnSubscribe<Throwable>() {
            @Override
            public void subscribe(final ObservableEmitter<Throwable> emitter) throws Exception {
                final ErrorObserver errorObserver = new ErrorObserver() {
                    @Override
                    public void onError(@NonNull Throwable e) {
                        emitter.onNext(e);
                    }
                };
                data.registerErrorObserver(errorObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        data.unregisterErrorObserver(errorObserver);
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
