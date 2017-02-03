package com.nextfaze.poweradapters.rx;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.nextfaze.poweradapters.Condition;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

final class ObservableCondition extends Condition {

    private static final Action1<Throwable> EMPTY_ERROR_HANDLER = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
        }
    };

    @NonNull
    private final Observable<Boolean> mObservable;

    @NonNull
    private final Handler mHandler = new Handler(getMainLooper());

    private boolean mValue;

    @Nullable
    private Subscription mSubscription;

    ObservableCondition(@NonNull Observable<Boolean> observable) {
        mObservable = checkNotNull(observable, "observable");
    }

    @Override
    public boolean eval() {
        return mValue;
    }

    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
        mSubscription = mObservable.subscribe(new Action1<Boolean>() {
            @Override
            public void call(final Boolean value) {
                setValue(value);
            }
        }, EMPTY_ERROR_HANDLER);
    }

    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }

    @SuppressWarnings("WeakerAccess")
    void setValue(final boolean value) {
        if (myLooper() == getMainLooper()) {
            if (value != mValue) {
                mValue = value;
                notifyChanged();
            }
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setValue(value);
                }
            });
        }
    }
}
