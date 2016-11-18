package com.nextfaze.poweradapters.data.rx;

import android.support.annotation.Nullable;
import com.nextfaze.poweradapters.data.Data;
import lombok.NonNull;
import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

import java.util.Collection;

public final class ObservableDataBuilder<T> {

    @Nullable
    private Observable<? extends Collection<? extends T>> mContents;

    @Nullable
    private Observable<? extends Collection<? extends T>> mPrepends;

    @Nullable
    private Observable<? extends Collection<? extends T>> mAppends;

    @Nullable
    private EqualityFunction<? super T> mIdentityEqualityFunction;

    @Nullable
    private EqualityFunction<? super T> mContentEqualityFunction = new EqualityFunction<T>() {
        @Override
        public boolean equal(@NonNull T a, @NonNull T b) {
            return a.equals(b);
        }
    };

    private boolean mDetectMoves = true;

    @Nullable
    private Observable<Boolean> mLoading;

    public ObservableDataBuilder() {
    }

    /** Each emission of this observables overwrites the elements of the data. */
    @NonNull
    public ObservableDataBuilder<T> contents(@Nullable Observable<? extends Collection<? extends T>> contents) {
        mContents = contents;
        return this;
    }

    /** Each emission of this observables prepends to the elements of the data. */
    @NonNull
    public ObservableDataBuilder<T> prepends(@Nullable Observable<? extends Collection<? extends T>> prepends) {
        mPrepends = prepends;
        return this;
    }

    /** Each emission of this observables appends to the elements of the data. */
    @NonNull
    public ObservableDataBuilder<T> appends(@Nullable Observable<? extends Collection<? extends T>> appends) {
        mAppends = appends;
        return this;
    }

    /**
     * Sets the observable that will control the loading state of the resulting data. If not specified, the resulting
     * data considers itself in a loading state in between subscription to {@code contents}, and the first emission of
     * {@link #contents(Observable)}.
     */
    @NonNull
    public ObservableDataBuilder<T> loading(@Nullable Observable<Boolean> loading) {
        mLoading = loading;
        return this;
    }

    @NonNull
    public ObservableDataBuilder<T> identityEquality(@Nullable EqualityFunction<? super T> identityEqualityFunction) {
        mIdentityEqualityFunction = identityEqualityFunction;
        return this;
    }

    @NonNull
    public ObservableDataBuilder<T> contentEquality(@Nullable EqualityFunction<? super T> contentEqualityFunction) {
        mContentEqualityFunction = contentEqualityFunction;
        return this;
    }

    @NonNull
    public ObservableDataBuilder<T> detectMoves(boolean detectMoves) {
        mDetectMoves = detectMoves;
        return this;
    }

    @NonNull
    public Data<T> build() {
        if (mLoading == null) {
            PublishSubject<Boolean> loadingSubject = PublishSubject.create();
            return new ObservableData<>(
                    considerAsLoadingUntilFirstEmission(mContents, loadingSubject),
                    considerAsLoadingUntilFirstEmission(mPrepends, loadingSubject),
                    considerAsLoadingUntilFirstEmission(mAppends, loadingSubject),
                    loadingSubject,
                    mIdentityEqualityFunction,
                    mContentEqualityFunction,
                    mDetectMoves
            );
        }
        return new ObservableData<>(
                mContents,
                mPrepends,
                mAppends,
                mLoading,
                mIdentityEqualityFunction,
                mContentEqualityFunction,
                mDetectMoves
        );
    }

    @Nullable
    private <E> Observable<E> considerAsLoadingUntilFirstEmission(@Nullable Observable<E> observable,
                                                                  @NonNull final Observer<Boolean> observer) {
        if (observable == null) {
            return null;
        }
        return observable
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        observer.onNext(true);
                    }
                })
                .doOnNext(new Action1<E>() {
                    @Override
                    public void call(E e) {
                        observer.onNext(false);
                    }
                })
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        observer.onNext(false);
                    }
                });
    }

    public interface EqualityFunction<T> {
        boolean equal(@NonNull T a, @NonNull T b);
    }
}
