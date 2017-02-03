package com.nextfaze.poweradapters.data;

import android.support.annotation.NonNull;
import com.nextfaze.poweradapters.Condition;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.Predicate;
import com.nextfaze.poweradapters.SimpleDataObserver;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

final class DataCondition<T> extends Condition {

    @NonNull
    private final DataObserver mDataObserver = new SimpleDataObserver() {
        @Override
        public void onChanged() {
            notifyChanged();
        }
    };

    @NonNull
    private final LoadingObserver mLoadingObserver = new LoadingObserver() {
        @Override
        public void onLoadingChange() {
            DataCondition.this.notifyChanged();
        }
    };

    @NonNull
    private final AvailableObserver mAvailableObserver = new AvailableObserver() {
        @Override
        public void onAvailableChange() {
            notifyChanged();
        }
    };

    @NonNull
    private final ErrorObserver mErrorObserver = new ErrorObserver() {
        @Override
        public void onError(@NonNull Throwable e) {
            notifyChanged();
        }
    };

    @NonNull
    private final Data<? extends T> mData;

    @NonNull
    private final Predicate<? super Data<? extends T>> mPredicate;

    DataCondition(@NonNull Data<? extends T> data, @NonNull Predicate<? super Data<? extends T>> predicate) {
        mPredicate = checkNotNull(predicate, "predicate");
        mData = checkNotNull(data, "data");
    }

    @Override
    public boolean eval() {
        return mPredicate.apply(mData);
    }

    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mData.registerDataObserver(mDataObserver);
        mData.registerLoadingObserver(mLoadingObserver);
        mData.registerAvailableObserver(mAvailableObserver);
        mData.registerErrorObserver(mErrorObserver);
    }

    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mData.unregisterDataObserver(mDataObserver);
        mData.unregisterLoadingObserver(mLoadingObserver);
        mData.unregisterAvailableObserver(mAvailableObserver);
        mData.unregisterErrorObserver(mErrorObserver);
    }
}
