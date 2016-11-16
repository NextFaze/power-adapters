package com.nextfaze.poweradapters.data.rx;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import com.nextfaze.poweradapters.data.Data;
import lombok.NonNull;
import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.lang.Math.min;
import static rx.Observable.empty;

public final class ObservableDataBuilder<T> {

    @Nullable
    private Observable<? extends Collection<? extends T>> mContents;

    @Nullable
    private Observable<? extends Collection<? extends T>> mPrepends;

    @Nullable
    private Observable<? extends Collection<? extends T>> mAppends;

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
    public Data<T> build() {
        if (mLoading == null) {
            PublishSubject<Boolean> loadingSubject = PublishSubject.create();
            return new ObservableData<>(
                    considerAsLoadingUntilFirstEmission(mContents, loadingSubject),
                    considerAsLoadingUntilFirstEmission(mPrepends, loadingSubject),
                    considerAsLoadingUntilFirstEmission(mAppends, loadingSubject),
                    loadingSubject
            );
        }
        Observable<Collection<? extends T>> empty = empty();
        return new ObservableData<>(
                mContents != null ? mContents : empty,
                mPrepends != null ? mPrepends : empty,
                mAppends != null ? mAppends : empty,
                mLoading
        );
    }

    @NonNull
    private <E> Observable<E> considerAsLoadingUntilFirstEmission(@Nullable Observable<E> observable,
                                                                  @NonNull final Observer<Boolean> observer) {
        if (observable == null) {
            return empty();
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

    @SuppressWarnings("WeakerAccess")
    static final class ObservableData<T> extends Data<T> {

        @NonNull
        private final ArrayList<T> mData = new ArrayList<>();

        @NonNull
        private final Observable<? extends Collection<? extends T>> mContentsObservable;

        @NonNull
        private final Observable<? extends Collection<? extends T>> mPrependsObservable;

        @NonNull
        private final Observable<? extends Collection<? extends T>> mAppendsObservable;

        @NonNull
        private final Observable<Boolean> mLoadingObservable;

        @NonNull
        private final CompositeSubscription mSubscriptions = new CompositeSubscription();

        private boolean mClear;

        boolean mLoading;

        int mAvailable = Integer.MAX_VALUE;

        ObservableData(@NonNull Observable<? extends Collection<? extends T>> contents,
                       @NonNull Observable<? extends Collection<? extends T>> prepends,
                       @NonNull Observable<? extends Collection<? extends T>> appends,
                       @NonNull Observable<Boolean> loading) {
            mContentsObservable = contents;
            mPrependsObservable = prepends;
            mAppendsObservable = appends;
            mLoadingObservable = loading;
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
                Action1<Throwable> onError = new Action1<Throwable>() {
                    @Override
                    public void call(Throwable error) {
                        onObservableError(error);
                    }
                };
                Action0 onCompleted = new Action0() {
                    @Override
                    public void call() {
                        onObservableComplete();
                    }
                };

                // Loading must be subscribed to first.
                mSubscriptions.add(mLoadingObservable
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean l) {
                                setLoading(l != null && l);
                            }
                        }, onError));

                // Content
                mSubscriptions.add(mContentsObservable.subscribe(new Action1<Collection<? extends T>>() {
                    @Override
                    public void call(final Collection<? extends T> contents) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                overwriteContents(contents);
                            }
                        });
                    }
                }, onError, onCompleted));

                // Prepends
                mSubscriptions.add(mPrependsObservable.subscribe(new Action1<Collection<? extends T>>() {
                    @Override
                    public void call(final Collection<? extends T> contents) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                prependContents(contents);
                            }
                        });
                    }
                }, onError, onCompleted));

                // Appends
                mSubscriptions.add(mAppendsObservable.subscribe(new Action1<Collection<? extends T>>() {
                    @Override
                    public void call(final Collection<? extends T> contents) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                appendContents(contents);
                            }
                        });
                    }
                }, onError, onCompleted));
            }
        }

        private void unsubscribe() {
            mSubscriptions.clear();
        }

        void onObservableError(@NonNull Throwable error) {
            notifyError(error);
        }

        void onObservableComplete() {
            setAvailable(0);
        }

        void overwriteContents(@Nullable Collection<? extends T> contents) {
            if (contents == null) {
                contents = Collections.emptyList();
            }
            int oldSize = mData.size();
            mData.clear();
            mData.addAll(contents);
            int newSize = contents.size();
            int deltaSize = newSize - oldSize;
            int changed = min(oldSize, newSize);
            if (changed > 0) {
                notifyItemRangeChanged(0, changed);
            }
            if (deltaSize < 0) {
                notifyItemRangeRemoved(oldSize + deltaSize, -deltaSize);
            } else if (deltaSize > 0) {
                notifyItemRangeInserted(oldSize, deltaSize);
            }
            setAvailable(0);
        }

        void prependContents(@Nullable Collection<? extends T> contents) {
            if (contents == null) {
                return;
            }
            if (contents.size() > 0) {
                mData.addAll(0, contents);
                notifyItemRangeInserted(0, contents.size());
            }
            setAvailable(0);
        }

        void appendContents(@Nullable Collection<? extends T> contents) {
            if (contents == null) {
                return;
            }
            if (contents.size() > 0) {
                int oldSize = mData.size();
                mData.addAll(contents);
                notifyItemRangeInserted(oldSize, contents.size());
            }
            setAvailable(0);
        }

        void clear() {
            mData.clear();
            int size = mData.size();
            if (size > 0) {
                mData.clear();
                setAvailable(Integer.MAX_VALUE);
                notifyItemRangeRemoved(0, size);
            }
            mClear = false;
        }

        void setLoading(final boolean loading) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mLoading != loading) {
                        mLoading = loading;
                        ObservableData.this.notifyLoadingChanged();
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
                        ObservableData.this.notifyAvailableChanged();
                    }
                }
            });
        }

        @NonNull
        @Override
        public T get(int position, int flags) {
            return mData.get(position);
        }

        @Override
        public int size() {
            return mData.size();
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
}
