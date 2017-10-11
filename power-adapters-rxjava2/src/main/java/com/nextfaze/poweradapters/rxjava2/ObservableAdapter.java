package com.nextfaze.poweradapters.rxjava2;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nextfaze.poweradapters.binding.BindingAdapter;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.rxjava2.internal.DiffList;

import java.util.Collection;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;

@SuppressWarnings("WeakerAccess")
final class ObservableAdapter<T> extends BindingAdapter<T> {

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

    @NonNull
    final Mapper<? super T> mMapper;

    @Nullable
    final Observable<? extends Collection<? extends T>> mContentsObservable;

    @Nullable
    final Observable<? extends Collection<? extends T>> mPrependsObservable;

    @Nullable
    final Observable<? extends Collection<? extends T>> mAppendsObservable;

    ObservableAdapter(@NonNull Mapper<? super T> mapper,
                      @Nullable Observable<? extends Collection<? extends T>> contentsObservable,
                      @Nullable Observable<? extends Collection<? extends T>> prependsObservable,
                      @Nullable Observable<? extends Collection<? extends T>> appendsObservable,
                      @Nullable EqualityFunction<? super T> identityEqualityFunction,
                      @Nullable EqualityFunction<? super T> contentEqualityFunction,
                      boolean detectMoves) {
        super(mapper);
        mMapper = mapper;
        mContentsObservable = contentsObservable;
        mPrependsObservable = prependsObservable;
        mAppendsObservable = appendsObservable;
        mList = new DiffList<>(getDataObservable(), identityEqualityFunction, contentEqualityFunction, detectMoves);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @NonNull
    @Override
    protected T getItem(int position) {
        return mList.get(position);
    }

    @CallSuper
    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        subscribeIfAppropriate();
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        unsubscribe();
    }

    private void subscribeIfAppropriate() {
        if (getObserverCount() > 0 && mDisposables.size() <= 0) {
            Consumer<Object> onNext = EMPTY_CONSUMER;
            Consumer<Throwable> onError = new Consumer<Throwable>() {
                @Override
                public void accept(Throwable error) {
                    RxJavaPlugins.onError(error);
                }
            };
            Action onCompleted = EMPTY_ACTION;

            // Content
            if (mContentsObservable != null) {
                mDisposables.add(mContentsObservable.switchMap(new Function<Collection<? extends T>, Observable<?>>() {
                    @Override
                    public Observable<?> apply(Collection<? extends T> contents) {
                        return mList.overwrite(contents).toObservable();
                    }
                }).subscribe(onNext, onError, onCompleted));
            }

            // Prepends
            if (mPrependsObservable != null) {
                mDisposables.add(mPrependsObservable.switchMap(new Function<Collection<? extends T>, Observable<?>>() {
                    @Override
                    public Observable<?> apply(Collection<? extends T> contents) {
                        return mList.prepend(contents).toObservable();
                    }
                }).subscribe(onNext, onError, onCompleted));
            }

            // Appends
            if (mAppendsObservable != null) {
                mDisposables.add(mAppendsObservable.switchMap(new Function<Collection<? extends T>, Observable<?>>() {
                    @Override
                    public Observable<?> apply(Collection<? extends T> contents) {
                        return mList.append(contents).toObservable();
                    }
                }).subscribe(onNext, onError, onCompleted));
            }
        }
    }

    private void unsubscribe() {
        mDisposables.clear();
    }
}
