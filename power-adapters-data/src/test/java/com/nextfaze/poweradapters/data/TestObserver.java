package com.nextfaze.poweradapters.data;

import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.Predicate;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.collect.Lists.newArrayList;

/**
 * An observer designed for testing {@link Data} objects.
 * <ul>
 * <li>Ensures that notifications emitted by the specified {@link Data} are consistent with its {@link Data#size()}
 * return value.</li>
 * <li>Aids testing multithreaded implementations.</li>
 * </ul>
 */
@SuppressWarnings("WeakerAccess")
final class TestObserver<T> implements DataObserver, LoadingObserver, AvailableObserver {

    private int mShadowSize;

    @NonNull
    final Data<T> mData;

    @NonNull
    final List<Boolean> mLoadingEvents = new ArrayList<>();

    @NonNull
    final List<List<T>> mChangeEvents = new ArrayList<>();

    @NonNull
    final List<Integer> mAvailableEvents = new ArrayList<>();

    @NonNull
    private final Lock mLock = new ReentrantLock();

    @NonNull
    private final Condition mCondition = mLock.newCondition();

    TestObserver(@NonNull Data<T> data) {
        mData = data;
        mShadowSize = mData.size();
    }

    @Override
    public void onChanged() {
        mShadowSize = mData.size();
        addChangeEvent();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        addChangeEvent();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        mShadowSize += itemCount;
        addChangeEvent();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        mShadowSize -= itemCount;
        addChangeEvent();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        addChangeEvent();
    }

    private void addChangeEvent() {
        mLock.lock();
        try {
            mChangeEvents.add(new ArrayList<>(mData.asList()));
            mCondition.signal();
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public void onLoadingChange() {
        mLock.lock();
        try {
            mLoadingEvents.add(mData.isLoading());
            mCondition.signal();
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public void onAvailableChange() {
        mLock.lock();
        try {
            mAvailableEvents.add(mData.available());
            mCondition.signal();
        } finally {
            mLock.unlock();
        }
    }

    void awaitLoadingEvents(@NonNull Boolean... loadingEvents) {
        final List<Boolean> expected = newArrayList(loadingEvents);
        waitUntilConditionIsTrue(new Predicate<Void>() {
            @Override
            public boolean apply(Void v) {
                return mLoadingEvents.equals(expected);
            }
        });
    }

    @SafeVarargs
    final void awaitChangeEvents(@NonNull List<T>... changeEvents) {
        final List<List<T>> expected = newArrayList(changeEvents);
        waitUntilConditionIsTrue(new Predicate<Void>() {
            @Override
            public boolean apply(Void v) {
                return mChangeEvents.equals(expected);
            }
        });
    }

    void awaitAvailableEvents(@NonNull Integer... availableEvents) {
        final List<Integer> expected = newArrayList(availableEvents);
        waitUntilConditionIsTrue(new Predicate<Void>() {
            @Override
            public boolean apply(Void v) {
                return mAvailableEvents.equals(expected);
            }
        });
    }

    void awaitContent(@NonNull final List<T> content) {
        waitUntilConditionIsTrue(new Predicate<Void>() {
            @Override
            public boolean apply(Void v) {
                return mData.asList().equals(content);
            }
        });
    }

    private void waitUntilConditionIsTrue(@NonNull Predicate<Void> condition) {
        mLock.lock();
        try {
            while (!condition.apply(null)) {
                mCondition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            mLock.unlock();
        }
    }

    void assertNotificationsConsistent() {
        int actualSize = mData.size();
        if (mShadowSize != actualSize) {
            throw new AssertionError("Inconsistency detected: expected size " + mShadowSize + " but it is " + actualSize);
        }
    }
}
