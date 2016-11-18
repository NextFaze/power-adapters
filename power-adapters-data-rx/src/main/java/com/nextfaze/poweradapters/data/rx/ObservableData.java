package com.nextfaze.poweradapters.data.rx;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.rx.ObservableDataBuilder.EqualityFunction;
import lombok.NonNull;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import static android.support.v7.util.DiffUtil.calculateDiff;
import static java.lang.Math.min;
import static rx.Observable.fromCallable;
import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.computation;

@SuppressWarnings("WeakerAccess")
final class ObservableData<T> extends Data<T> {

    @NonNull
    final ArrayList<T> mData = new ArrayList<>();

    @NonNull
    final CompositeSubscription mSubscriptions = new CompositeSubscription();

    @Nullable
    final Observable<? extends Collection<? extends T>> mContentsObservable;

    @Nullable
    final Observable<? extends Collection<? extends T>> mPrependsObservable;

    @Nullable
    final Observable<? extends Collection<? extends T>> mAppendsObservable;

    @Nullable
    final EqualityFunction<? super T> mIdentityEqualityFunction;

    @Nullable
    final EqualityFunction<? super T> mContentEqualityFunction;

    @NonNull
    final Observable<Boolean> mLoadingObservable;

    @NonNull
    final ListUpdateCallback mListUpdateCallback = new ListUpdateCallback() {
        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count, Object payload) {
            notifyItemRangeChanged(position, count);
        }
    };

    final boolean mDetectMoves;

    private boolean mClear;

    boolean mLoading;

    int mAvailable = Integer.MAX_VALUE;

    ObservableData(@Nullable Observable<? extends Collection<? extends T>> contents,
                   @Nullable Observable<? extends Collection<? extends T>> prepends,
                   @Nullable Observable<? extends Collection<? extends T>> appends,
                   @NonNull Observable<Boolean> loading,
                   @Nullable EqualityFunction<? super T> identityEqualityFunction,
                   @Nullable EqualityFunction<? super T> contentEqualityFunction, boolean detectMoves) {
        mContentsObservable = contents;
        mPrependsObservable = prepends;
        mAppendsObservable = appends;
        mLoadingObservable = loading;
        mIdentityEqualityFunction = identityEqualityFunction;
        mContentEqualityFunction = contentEqualityFunction;
        mDetectMoves = detectMoves;
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
                    onObservableError(error);
                }
            };
            Action0 onCompleted = new Action0() {
                @Override
                public void call() {
                    onObservableComplete();
                }
            };

            // Loading must be subscribed to first
            mSubscriptions.add(mLoadingObservable.subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean l) {
                    // Treat null as false
                    setLoading(l != null && l);
                }
            }, onError));

            // Content
            if (mContentsObservable != null) {
                mSubscriptions.add(mContentsObservable.switchMap(new Func1<Collection<? extends T>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Collection<? extends T> contents) {
                        return overwrite(contents);
                    }
                }).subscribe(onNext, onError, onCompleted));
            }

            // Prepends
            if (mPrependsObservable != null) {
                mSubscriptions.add(mPrependsObservable.switchMap(new Func1<Collection<? extends T>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Collection<? extends T> contents) {
                        return prepend(contents);
                    }
                }).subscribe(onNext, onError, onCompleted));
            }

            // Appends
            if (mAppendsObservable != null) {
                mSubscriptions.add(mAppendsObservable.switchMap(new Func1<Collection<? extends T>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Collection<? extends T> contents) {
                        return append(contents);
                    }
                }).subscribe(onNext, onError, onCompleted));
            }
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

    @NonNull
    Observable<?> prepend(@NonNull final Collection<? extends T> list) {
        return mainThreadObservable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mData.addAll(0, list);
                notifyItemRangeInserted(0, list.size());
                setAvailable(0);
                return null;
            }
        });
    }

    @NonNull
    Observable<?> append(@NonNull final Collection<? extends T> list) {
        return mainThreadObservable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                int oldSize = mData.size();
                mData.addAll(list);
                notifyItemRangeInserted(oldSize, list.size());
                setAvailable(0);
                return null;
            }
        });
    }

    @NonNull
    Observable<?> overwriteBasic(@NonNull final Collection<? extends T> collection) {
        return mainThreadObservable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final List<T> existing = mData;
                final int oldSize = existing.size();
                existing.clear();
                existing.addAll(collection);
                final int newSize = collection.size();
                final int deltaSize = newSize - oldSize;
                final int changed = min(oldSize, newSize);
                if (changed > 0) {
                    notifyItemRangeChanged(0, changed);
                }
                if (deltaSize < 0) {
                    notifyItemRangeRemoved(oldSize + deltaSize, -deltaSize);
                } else if (deltaSize > 0) {
                    notifyItemRangeInserted(oldSize, deltaSize);
                }
                return null;
            }
        });
    }

    @NonNull
    Observable<?> overwrite(@NonNull Collection<? extends T> collection) {
        if (mIdentityEqualityFunction == null || mContentEqualityFunction == null) {
            return overwriteBasic(collection);
        }
        return overwriteUsingDiffUtil(new ArrayList<>(collection), mIdentityEqualityFunction,
                mContentEqualityFunction);
    }

    @NonNull
    Observable<?> overwriteUsingDiffUtil(@NonNull final List<? extends T> list,
                                         @NonNull final EqualityFunction<? super T> identityEqualityFunction,
                                         @NonNull final EqualityFunction<? super T> contentEqualityFunction) {
        final List<T> existing = new ArrayList<>(mData);
        return fromCallable(new Callable<DiffUtil.DiffResult>() {
            @Override
            public DiffUtil.DiffResult call() throws Exception {
                DiffUtil.Callback callback = new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return existing.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return list.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return identityEqualityFunction
                                .equal(existing.get(oldItemPosition), list.get(newItemPosition));
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        return contentEqualityFunction
                                .equal(existing.get(oldItemPosition), list.get(newItemPosition));
                    }
                };
                return calculateDiff(callback, mDetectMoves);
            }
        }).subscribeOn(computation()).flatMap(new Func1<DiffUtil.DiffResult, Observable<?>>() {
            @Override
            public Observable<?> call(final DiffUtil.DiffResult result) {
                return mainThreadObservable(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        mData.clear();
                        mData.addAll(list);
                        result.dispatchUpdatesTo(mListUpdateCallback);
                        setAvailable(0);
                        return null;
                    }
                });
            }
        });
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

    @NonNull
    static <T> Observable<T> mainThreadObservable(@NonNull Callable<T> callable) {
        return fromCallable(callable).subscribeOn(mainThread());
    }
}
