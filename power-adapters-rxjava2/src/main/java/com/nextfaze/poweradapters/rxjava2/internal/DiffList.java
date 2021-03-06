package com.nextfaze.poweradapters.rxjava2.internal;

import com.nextfaze.poweradapters.internal.DataObservable;
import com.nextfaze.poweradapters.rxjava2.EqualityFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;
import static com.nextfaze.poweradapters.rxjava2.internal.Utils.mainThreadCompletable;
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
    public Completable prepend(@NonNull final Collection<? extends T> list) {
        checkNotNull(list, "list");
        return mainThreadCompletable(new Action() {
            @Override
            public void run() throws Exception {
                mData.addAll(0, list);
                mDataObservable.notifyItemRangeInserted(0, list.size());
            }
        });
    }

    @NonNull
    public Completable append(@NonNull final Collection<? extends T> list) {
        checkNotNull(list, "list");
        return mainThreadCompletable(new Action() {
            @Override
            public void run() throws Exception {
                int oldSize = mData.size();
                mData.addAll(list);
                mDataObservable.notifyItemRangeInserted(oldSize, list.size());
            }
        });
    }

    @NonNull
    public Completable overwrite(@NonNull Collection<? extends T> collection) {
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
    Completable overwriteBasic(@NonNull final Collection<? extends T> collection) {
        return mainThreadCompletable(new Action() {
            @Override
            public void run() throws Exception {
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
                    mDataObservable.notifyItemRangeChanged(0, changed, null);
                }
            }
        });
    }

    // TODO: Fix race condition between async diff util overwrites and the others.

    @NonNull
    Completable overwriteUsingDiffUtil(@NonNull final List<? extends T> newContents,
                                         @NonNull final EqualityFunction<? super T> identityEqualityFunction,
                                         @NonNull final EqualityFunction<? super T> contentEqualityFunction) {
        return copyContents().switchMap(new Function<List<? extends T>, Observable<?>>() {
            @Override
            public Observable<Object> apply(List<? extends T> existingContentsCopy) throws Exception {
                return calculateDiff(existingContentsCopy, newContents, identityEqualityFunction, contentEqualityFunction)
                        .switchMap(new Function<DiffUtil.DiffResult, Observable<Object>>() {
                            @Override
                            public Observable<Object> apply(DiffUtil.DiffResult diffResult) throws Exception {
                                return applyNewContentsAndDispatchDiffNotifications(newContents, diffResult).toObservable();
                            }
                        });
            }
        }).ignoreElements();
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
    Completable applyNewContentsAndDispatchDiffNotifications(@NonNull final List<? extends T> newContents,
                                                             @NonNull final DiffUtil.DiffResult diffResult) {
        return mainThreadCompletable(new Action() {
            @Override
            public void run() throws Exception {
                mData.clear();
                mData.addAll(newContents);
                diffResult.dispatchUpdatesTo(mListUpdateCallback);
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
