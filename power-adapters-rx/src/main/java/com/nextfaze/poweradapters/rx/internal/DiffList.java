package com.nextfaze.poweradapters.rx.internal;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import com.nextfaze.poweradapters.internal.DataObservable;
import com.nextfaze.poweradapters.rx.EqualityFunction;
import lombok.NonNull;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import static android.support.v7.util.DiffUtil.calculateDiff;
import static com.nextfaze.poweradapters.rx.internal.Utils.mainThreadObservable;
import static java.lang.Math.min;
import static rx.Observable.fromCallable;
import static rx.schedulers.Schedulers.computation;

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
        mData.clear();
        int size = mData.size();
        if (size > 0) {
            mData.clear();
            mDataObservable.notifyItemRangeRemoved(0, size);
        }
    }

    @NonNull
    public Observable<?> prepend(@NonNull final Collection<? extends T> list) {
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
        if (mIdentityEqualityFunction == null || mContentEqualityFunction == null) {
            return overwriteBasic(collection);
        }
        return overwriteUsingDiffUtil(new ArrayList<>(collection), mIdentityEqualityFunction,
                mContentEqualityFunction);
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
                        return null;
                    }
                });
            }
        });
    }

}
