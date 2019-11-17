package com.nextfaze.poweradapters.rxjava2;

import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.SimpleDataObserver;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.MainThreadDisposable;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;
import static io.reactivex.android.MainThreadDisposable.verifyMainThread;

public final class RxPowerAdapter {

    private RxPowerAdapter() {
        throw new AssertionError();
    }

    @CheckResult
    @NonNull
    public static Observable<Integer> itemCount(@NonNull final PowerAdapter adapter) {
        checkNotNull(adapter, "adapter");
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(final ObservableEmitter<Integer> emitter) throws Exception {
                verifyMainThread();
                final DataObserver dataObserver = new Observer() {
                    @Override
                    public void onChanged() {
                        emitter.onNext(adapter.getItemCount());
                    }
                };
                emitter.onNext(adapter.getItemCount());
                adapter.registerDataObserver(dataObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        adapter.unregisterDataObserver(dataObserver);
                    }
                });
            }
        }).distinctUntilChanged();
    }

    @CheckResult
    @NonNull
    public static Observable<ChangeEvent> changes(@NonNull final PowerAdapter adapter) {
        checkNotNull(adapter, "adapter");
        return Observable.create(new ObservableOnSubscribe<ChangeEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<ChangeEvent> emitter) throws Exception {
                verifyMainThread();
                final DataObserver dataObserver = new Observer() {
                    @Override
                    public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                        emitter.onNext(new ChangeEvent(positionStart, itemCount, payload));
                    }
                };
                adapter.registerDataObserver(dataObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        adapter.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<InsertEvent> inserts(@NonNull final PowerAdapter adapter) {
        checkNotNull(adapter, "adapter");
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
                adapter.registerDataObserver(dataObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        adapter.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<RemoveEvent> removes(@NonNull final PowerAdapter adapter) {
        checkNotNull(adapter, "adapter");
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
                adapter.registerDataObserver(dataObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        adapter.unregisterDataObserver(dataObserver);
                    }
                });
            }
        });
    }

    @CheckResult
    @NonNull
    public static Observable<MoveEvent> moves(@NonNull final PowerAdapter adapter) {
        checkNotNull(adapter, "adapter");
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
                adapter.registerDataObserver(dataObserver);
                emitter.setDisposable(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
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
