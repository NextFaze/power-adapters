package com.nextfaze.poweradapters.rx;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import com.nextfaze.poweradapters.binding.BindingAdapter;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.rx.internal.DiffList;
import lombok.NonNull;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

import java.util.Collection;

@SuppressWarnings("WeakerAccess")
final class ObservableAdapter<T> extends BindingAdapter<T> {

    @NonNull
    final DiffList<T> mList;

    @NonNull
    final CompositeSubscription mSubscriptions = new CompositeSubscription();

    @NonNull
    final Mapper mMapper;

    @Nullable
    final Observable<? extends Collection<? extends T>> mContentsObservable;

    @Nullable
    final Observable<? extends Collection<? extends T>> mPrependsObservable;

    @Nullable
    final Observable<? extends Collection<? extends T>> mAppendsObservable;

    ObservableAdapter(@NonNull Mapper mapper,
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
        if (getObserverCount() > 0 && !mSubscriptions.hasSubscriptions()) {
            Action1<Object> onNext = Actions.empty();
            Action1<Throwable> onError = new Action1<Throwable>() {
                @Override
                public void call(Throwable error) {
                    // Ignore
                }
            };
            Action0 onCompleted = Actions.empty();

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
}
