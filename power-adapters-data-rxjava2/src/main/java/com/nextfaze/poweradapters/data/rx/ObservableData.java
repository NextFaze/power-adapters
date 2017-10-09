package com.nextfaze.poweradapters.data.rx;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.rxjava2.EqualityFunction;
import com.nextfaze.poweradapters.rxjava2.internal.DiffList;

import java.util.Collection;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

@SuppressWarnings("WeakerAccess")
final class ObservableData<T> extends Data<T> {

    @NonNull
    private static final Consumer<Object> EMPTY_CONSUMER = new Consumer<Object>() {
        @Override
        public void accept(Object o) throws Exception {
        }
    };

    @NonNull
    private static final Action EMPTY_ACTION = new Action() {
        @Override
        public void run() throws Exception {
        }
    };

    @NonNull
    final DiffList<T> mList;

    @NonNull
    final CompositeDisposable mDisposables = new CompositeDisposable();

    @Nullable
    final Observable<? extends Collection<? extends T>> mContentsObservable;

    @Nullable
    final Observable<? extends Collection<? extends T>> mPrependsObservable;

    @Nullable
    final Observable<? extends Collection<? extends T>> mAppendsObservable;

    @NonNull
    final Observable<Integer> mAvailableObservable;

    @NonNull
    final Observable<Boolean> mLoadingObservable;

    @NonNull
    final Observable<Throwable> mErrorObservable;

    private boolean mClear;

    boolean mLoading;

    int mAvailable = Integer.MAX_VALUE;

    ObservableData(@Nullable Observable<? extends Collection<? extends T>> contentsObservable,
                   @Nullable Observable<? extends Collection<? extends T>> prependsObservable,
                   @Nullable Observable<? extends Collection<? extends T>> appendsObservable,
                   @NonNull Observable<Integer> availableObservable,
                   @NonNull Observable<Boolean> loadingObservable,
                   @NonNull Observable<Throwable> errorObservable,
                   @Nullable EqualityFunction<? super T> identityEqualityFunction,
                   @Nullable EqualityFunction<? super T> contentEqualityFunction,
                   boolean detectMoves) {
        mContentsObservable = contentsObservable;
        mPrependsObservable = prependsObservable;
        mAppendsObservable = appendsObservable;
        mAvailableObservable = availableObservable;
        mLoadingObservable = loadingObservable;
        mErrorObservable = errorObservable;
        mList = new DiffList<>(getDataObservable(), identityEqualityFunction, contentEqualityFunction, detectMoves);
    }

    @CallSuper
    @Override
    protected void onFirstDataObserverRegistered() {
        super.onFirstDataObserverRegistered();
        if (mClear) {
            clear();
        }
        subscribeIfAppropriate();
    }

    @CallSuper
    @Override
    protected void onLastDataObserverUnregistered() {
        super.onLastDataObserverUnregistered();
        unsubscribe();
    }

    private void subscribeIfAppropriate() {
        if (getDataObserverCount() > 0 && mDisposables.size() <= 0) {
            Consumer<Object> onNext = EMPTY_CONSUMER;
            Consumer<Throwable> onError = new Consumer<Throwable>() {
                @Override
                public void accept(Throwable error) throws Exception {
                    notifyError(error);
                }
            };
            Action onCompleted = EMPTY_ACTION;

            // Loading must be subscribed to first
            mDisposables.add(mLoadingObservable.subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean l) throws Exception {
                    // Treat null as false
                    setLoading(l != null && l);
                }
            }, onError, onCompleted));

            // Available
            mDisposables.add(mAvailableObservable.subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer available) throws Exception {
                    setAvailable(available);
                }
            }, onError, onCompleted));

            // Errors
            mDisposables.add(mErrorObservable.subscribe(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable e) throws Exception {
                    notifyError(e);
                }
            }));

            // Content
            if (mContentsObservable != null) {
                mDisposables.add(mContentsObservable.switchMap(new Function<Collection<? extends T>, Observable<?>>() {
                    @Override
                    public Observable<?> apply(Collection<? extends T> contents) throws Exception {
                        return mList.overwrite(contents).toObservable();
                    }
                }).subscribe(onNext, onError, onCompleted));
            }

            // Prepends
            if (mPrependsObservable != null) {
                mDisposables.add(mPrependsObservable.switchMap(new Function<Collection<? extends T>, Observable<?>>() {
                    @Override
                    public Observable<?> apply(Collection<? extends T> contents) throws Exception {
                        return mList.prepend(contents).toObservable();
                    }
                }).subscribe(onNext, onError, onCompleted));
            }

            // Appends
            if (mAppendsObservable != null) {
                mDisposables.add(mAppendsObservable.switchMap(new Function<Collection<? extends T>, Observable<?>>() {
                    @Override
                    public Observable<?> apply(Collection<? extends T> contents) throws Exception {
                        return mList.append(contents).toObservable();
                    }
                }).subscribe(onNext, onError, onCompleted));
            }
        }
    }

    private void unsubscribe() {
        mDisposables.clear();
    }

    void clear() {
        mList.clear();
        setAvailable(Integer.MAX_VALUE);
        mClear = false;
    }

    void setLoading(final boolean loading) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLoading != loading) {
                    mLoading = loading;
                    notifyLoadingChanged();
                }
            }
        });
    }

    void setAvailable(final int available) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAvailable != available) {
                    mAvailable = available;
                    notifyAvailableChanged();
                }
            }
        });
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        return mList.get(position);
    }

    @Override
    public int size() {
        return mList.size();
    }

    @Override
    public boolean isLoading() {
        return mLoading;
    }

    @Override
    public int available() {
        return mAvailable;
    }

    @Override
    public void invalidate() {
        unsubscribe();
        mClear = true;
    }

    @Override
    public void refresh() {
        unsubscribe();
        subscribeIfAppropriate();
    }

    @Override
    public void reload() {
        clear();
        refresh();
    }
}
