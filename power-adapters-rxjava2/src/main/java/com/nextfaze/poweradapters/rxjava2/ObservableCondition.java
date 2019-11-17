package com.nextfaze.poweradapters.rxjava2;

import android.os.Handler;

import com.nextfaze.poweradapters.Condition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

final class ObservableCondition extends Condition {

    @NonNull
    private final Observable<Boolean> mObservable;

    @NonNull
    private final Handler mHandler = new Handler(getMainLooper());

    private boolean mValue;

    @Nullable
    private Disposable mDisposable;

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
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = mObservable.subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(final Boolean value) {
                setValue(value);
            }
        });
    }

    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        if (mDisposable != null) {
            mDisposable.dispose();
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
