package com.nextfaze.poweradapters.data.rx;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.rxjava2.EqualityFunction;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

import java.util.Collection;

public final class ObservableDataBuilder<T> {

    @Nullable
    private Observable<? extends Collection<? extends T>> mContents;

    @Nullable
    private Observable<? extends Collection<? extends T>> mPrepends;

    @Nullable
    private Observable<? extends Collection<? extends T>> mAppends;

    @Nullable
    private Observable<Integer> mAvailable;

    @Nullable
    private Observable<Boolean> mLoading;

    @Nullable
    private Observable<Throwable> mErrors;

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

    public ObservableDataBuilder() {
    }

    /** Each emission of this observable overwrites the elements of the data. */
    @NonNull
    public ObservableDataBuilder<T> contents(@Nullable Observable<? extends Collection<? extends T>> contents) {
        mContents = contents;
        return this;
    }

    /** Each emission of this observable prepends to the elements of the data. */
    @NonNull
    public ObservableDataBuilder<T> prepends(@Nullable Observable<? extends Collection<? extends T>> prepends) {
        mPrepends = prepends;
        return this;
    }

    /** Each emission of this observable appends to the elements of the data. */
    @NonNull
    public ObservableDataBuilder<T> appends(@Nullable Observable<? extends Collection<? extends T>> appends) {
        mAppends = appends;
        return this;
    }

    /**
     * The {@link Data#available()} property will match the emissions of this observable.
     * <p>
     * If not specified, the resulting data will assume there are no more elements available after the first emission
     * of any of the content observables.
     * @see #contents(Observable)
     * @see #prepends(Observable)
     * @see #appends(Observable)
     */
    @NonNull
    public ObservableDataBuilder<T> available(@Nullable Observable<Integer> available) {
        mAvailable = available;
        return this;
    }

    /**
     * The {@link Data#isLoading()} property will match the emissions of this observable.
     * <p>
     * If not specified, the resulting data considers itself in a loading state until the first emission of any of the
     * content observables.
     * @see #contents(Observable)
     * @see #prepends(Observable)
     * @see #appends(Observable)
     */
    @NonNull
    public ObservableDataBuilder<T> loading(@Nullable Observable<Boolean> loading) {
        mLoading = loading;
        return this;
    }

    @NonNull
    public ObservableDataBuilder<T> errors(@Nullable Observable<Throwable> errors) {
        mErrors = errors;
        return this;
    }

    /**
     * Defines the function for evaluating whether two objects have the same identity, for the purpose of determining
     * notifications.
     */
    @NonNull
    public ObservableDataBuilder<T> identityEquality(@Nullable EqualityFunction<? super T> identityEqualityFunction) {
        mIdentityEqualityFunction = identityEqualityFunction;
        return this;
    }

    /**
     * Defines the function for evaluating whether two objects have the same contents, for the purpose of determining
     * notifications.
     * <p>
     * By default, content equality is evaluated using {@link Object#equals(Object)}.
     */
    @NonNull
    public ObservableDataBuilder<T> contentEquality(@Nullable EqualityFunction<? super T> contentEqualityFunction) {
        mContentEqualityFunction = contentEqualityFunction;
        return this;
    }

    /**
     * Sets whether the content diff engine should also detect item moves, as well as other changes.
     * <p>
     * This is {@code true} by default.
     */
    @NonNull
    public ObservableDataBuilder<T> detectMoves(boolean detectMoves) {
        mDetectMoves = detectMoves;
        return this;
    }

    @NonNull
    public Data<T> build() {
        Observable<? extends Collection<? extends T>> contents = mContents;
        Observable<? extends Collection<? extends T>> prepends = mPrepends;
        Observable<? extends Collection<? extends T>> appends = mAppends;
        Observable<Integer> available = mAvailable;
        Observable<Boolean> loading = mLoading;
        Observable<Throwable> errors = mErrors;
        if (available == null) {
            // If no available observable specified, assume no more available upon first emission of any content
            // observable.
            PublishSubject<Integer> availableSubject = PublishSubject.create();
            contents = changeToZeroAvailableUponFirstEmission(mContents, availableSubject);
            prepends = changeToZeroAvailableUponFirstEmission(mPrepends, availableSubject);
            appends = changeToZeroAvailableUponFirstEmission(mAppends, availableSubject);
            available = availableSubject;

        }
        if (loading == null) {
            // If no loading observable specified, assume loading has completed upon first emission of any content
            // observable.
            PublishSubject<Boolean> loadingSubject = PublishSubject.create();
            contents = considerAsLoadingUntilFirstEmission(mContents, loadingSubject);
            prepends = considerAsLoadingUntilFirstEmission(mPrepends, loadingSubject);
            appends = considerAsLoadingUntilFirstEmission(mAppends, loadingSubject);
            loading = loadingSubject;
        }
        if (errors == null) {
            errors = Observable.empty();
        }
        return new ObservableData<>(contents, prepends, appends, available, loading, errors, mIdentityEqualityFunction,
                mContentEqualityFunction, mDetectMoves);
    }

    @Nullable
    private <E> Observable<E> considerAsLoadingUntilFirstEmission(@Nullable Observable<E> observable,
                                                                  @NonNull final Observer<Boolean> observer) {
        if (observable == null) {
            return null;
        }
        return observable
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        observer.onNext(true);
                    }
                })
                .doOnNext(new Consumer<E>() {
                    @Override
                    public void accept(E e) throws Exception {
                        observer.onNext(false);
                    }
                })
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        observer.onNext(false);
                    }
                });
    }

    @Nullable
    private <E> Observable<E> changeToZeroAvailableUponFirstEmission(@Nullable Observable<E> observable,
                                                                     @NonNull final Observer<Integer> observer) {
        if (observable == null) {
            return null;
        }
        return observable
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        observer.onNext(Integer.MAX_VALUE);
                    }
                })
                .doOnNext(new Consumer<E>() {
                    @Override
                    public void accept(E e) throws Exception {
                        observer.onNext(0);
                    }
                })
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        observer.onNext(0);
                    }
                });
    }
}
