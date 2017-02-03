package com.nextfaze.poweradapters.data.rx;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.rx.EqualityFunction;
import com.nextfaze.poweradapters.rx.internal.DiffList;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

import java.util.Collection;

@SuppressWarnings("WeakerAccess")
final class ObservableData<T> extends Data<T> {

    @NonNull
    final DiffList<T> mList;

    @NonNull
    final CompositeSubscription mSubscriptions = new CompositeSubscription();

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
        if (getDataObserverCount() > 0 && !mSubscriptions.hasSubscriptions()) {
            Action1<Object> onNext = Actions.empty();
            Action1<Throwable> onError = new Action1<Throwable>() {
                @Override
                public void call(Throwable error) {
                    notifyError(error);
                }
            };
            Action0 onCompleted = Actions.empty();

            // Loading must be subscribed to first
            mSubscriptions.add(mLoadingObservable.subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean l) {
                    // Treat null as false
                    setLoading(l != null && l);
                }
            }, onError, onCompleted));

            // Available
            mSubscriptions.add(mAvailableObservable.subscribe(new Action1<Integer>() {
                @Override
                public void call(Integer available) {
                    setAvailable(available);
                }
            }, onError, onCompleted));

            // Errors
            mSubscriptions.add(mErrorObservable.subscribe(new Action1<Throwable>() {
                @Override
                public void call(Throwable e) {
                    notifyError(e);
                }
            }));

            // Content
            if (mContentsObservable != null) {
                mSubscriptions.add(mContentsObservable.switchMap(new Func1<Collection<? extends T>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Collection<? extends T> contents) {
                        return mList.overwrite(contents);
                    }
                }).subscribe(onNext, onError, onCompleted));
            }

            // Prepends
            if (mPrependsObservable != null) {
                mSubscriptions.add(mPrependsObservable.switchMap(new Func1<Collection<? extends T>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Collection<? extends T> contents) {
                        return mList.prepend(contents);
                    }
                }).subscribe(onNext, onError, onCompleted));
            }

            // Appends
            if (mAppendsObservable != null) {
                mSubscriptions.add(mAppendsObservable.switchMap(new Func1<Collection<? extends T>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Collection<? extends T> contents) {
                        return mList.append(contents);
                    }
                }).subscribe(onNext, onError, onCompleted));
            }
        }
    }

    private void unsubscribe() {
        mSubscriptions.clear();
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
