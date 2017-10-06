package com.nextfaze.poweradapters.rxjava2.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import com.nextfaze.poweradapters.internal.DataObservable;
import com.nextfaze.poweradapters.rxjava2.EqualityFunction;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;
import static com.nextfaze.poweradapters.rxjava2.internal.Utils.mainThreadObservable;
import static io.reactivex.schedulers.Schedulers.computation;
import static java.lang.Math.min;

/** For internal use only. */
@RestrictTo(LIBRARY_GROUP)
@SuppressWarnings("WeakerAccess")
public final class DiffList<T> {

    @NonNull
    final DataObservable mDataObservable;

    @NonNull
    final ArrayList<T> mData = new ArrayList<>();

    @Nullable
    final EqualityFunction<? super T> mIdentityEqualityFunction;

    @Nullable
    final EqualityFunction<? super T> mContentEqualityFunction;

    final boolean mDetectMoves;

    @NonNull
    final ListUpdateCallback mListUpdateCallback;

    public DiffList(@NonNull DataObservable dataObservable,
                    @Nullable EqualityFunction<? super T> identityEqualityFunction,
                    @Nullable EqualityFunction<? super T> contentEqualityFunction,
                    boolean detectMoves) {
        mDataObservable = dataObservable;
        mIdentityEqualityFunction = identityEqualityFunction;
        mContentEqualityFunction = contentEqualityFunction;
        mDetectMoves = detectMoves;
        mListUpdateCallback = new DataObservableListUpdateCallback(mDataObservable);
    }

    public int size() {
        return mData.size();
    }

    @NonNull
    public T get(int position) {
        return mData.get(position);
    }

    public void clear() {
        int size = mData.size();
        if (size > 0) {
            mData.clear();
            mDataObservable.notifyItemRangeRemoved(0, size);
        }
    }

    @NonNull
    public Observable<?> prepend(@NonNull final Collection<? extends T> list) {
        checkNotNull(list, "list");
        return mainThreadObservable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mData.addAll(0, list);
                mDataObservable.notifyItemRangeInserted(0, list.size());
                return null;
            }
        });
    }

    @NonNull
    public Observable<?> append(@NonNull final Collection<? extends T> list) {
        checkNotNull(list, "list");
        return mainThreadObservable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                int oldSize = mData.size();
                mData.addAll(list);
                mDataObservable.notifyItemRangeInserted(oldSize, list.size());
                return null;
            }
        });
    }

    @NonNull
    public Observable<?> overwrite(@NonNull Collection<? extends T> collection) {
        checkNotNull(collection, "collection");
        if (mIdentityEqualityFunction == null || mContentEqualityFunction == null) {
            return overwriteBasic(collection);
        }
        if (collection instanceof List) {
            //noinspection unchecked
            return overwriteUsingDiffUtil((List<? extends T>) collection,
                    mIdentityEqualityFunction, mContentEqualityFunction);
        }
        return overwriteUsingDiffUtil(new ArrayList<>(collection), mIdentityEqualityFunction, mContentEqualityFunction);
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

                // Issue removal/insertion notifications. These must happen first, otherwise downstream item count
                // verification will complain that our size has changed without a corresponding structural notification.
                if (deltaSize < 0) {
                    mDataObservable.notifyItemRangeRemoved(oldSize + deltaSize, -deltaSize);
                } else if (deltaSize > 0) {
                    mDataObservable.notifyItemRangeInserted(oldSize, deltaSize);
                }

                // Finally, issue a change notification for the range of elements not accounted for above.
                final int changed = min(oldSize, newSize);
                if (changed > 0) {
                    mDataObservable.notifyItemRangeChanged(0, changed);
                }
                return null;
            }
        });
    }

    // TODO: Fix race condition between async diff util overwrites and the others.

    @NonNull
    Observable<?> overwriteUsingDiffUtil(@NonNull final List<? extends T> newContents,
                                         @NonNull final EqualityFunction<? super T> identityEqualityFunction,
                                         @NonNull final EqualityFunction<? super T> contentEqualityFunction) {
        return copyContents().switchMap(new Function<List<? extends T>, Observable<?>>() {
            @Override
            public Observable<?> apply(List<? extends T> existingContentsCopy) throws Exception {
                return calculateDiff(existingContentsCopy, newContents, identityEqualityFunction, contentEqualityFunction)
                        .switchMap(new Function<DiffUtil.DiffResult, Observable<?>>() {
                            @Override
                            public Observable<?> apply(DiffUtil.DiffResult diffResult) throws Exception {
                                return applyNewContentsAndDispatchDiffNotifications(newContents, diffResult);
                            }
                        });
            }
        });
    }

    @NonNull
    Observable<List<? extends T>> copyContents() {
        return mainThreadObservable(new Callable<List<? extends T>>() {
            @Override
            public List<? extends T> call() throws Exception {
                return new ArrayList<>(mData);
            }
        });
    }

    @NonNull
    Observable<DiffUtil.DiffResult> calculateDiff(@NonNull final List<? extends T> oldContents,
                                                  @NonNull final List<? extends T> newContents,
                                                  @NonNull final EqualityFunction<? super T> identityEqualityFunction,
                                                  @NonNull final EqualityFunction<? super T> contentEqualityFunction) {
        return Observable.fromCallable(new Callable<DiffUtil.DiffResult>() {
            @Override
            public DiffUtil.DiffResult call() throws Exception {
                return diff(oldContents, newContents, identityEqualityFunction, contentEqualityFunction);
            }
        }).subscribeOn(computation());
    }

    @NonNull
    Observable<Void> applyNewContentsAndDispatchDiffNotifications(@NonNull final List<? extends T> newContents,
                                                                  @NonNull final DiffUtil.DiffResult diffResult) {
        return mainThreadObservable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mData.clear();
                mData.addAll(newContents);
                diffResult.dispatchUpdatesTo(mListUpdateCallback);
                return null;
            }
        });
    }

    @NonNull
    DiffUtil.DiffResult diff(@NonNull final List<? extends T> oldList,
                             @NonNull final List<? extends T> newList,
                             @NonNull final EqualityFunction<? super T> identityEqualityFunction,
                             @NonNull final EqualityFunction<? super T> contentEqualityFunction) {
        return DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return identityEqualityFunction
                        .equal(oldList.get(oldItemPosition), newList.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return contentEqualityFunction
                        .equal(oldList.get(oldItemPosition), newList.get(newItemPosition));
            }
        }, mDetectMoves);
    }
}
