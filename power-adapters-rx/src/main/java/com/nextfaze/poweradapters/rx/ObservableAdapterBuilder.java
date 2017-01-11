package com.nextfaze.poweradapters.rx;

import android.support.annotation.Nullable;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.binding.Mapper;
import lombok.NonNull;
import rx.Observable;

import java.util.Collection;

import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;

public final class ObservableAdapterBuilder<T> {

    @NonNull
    private final Mapper mMapper;

    @Nullable
    private Observable<? extends Collection<? extends T>> mContents;

    @Nullable
    private Observable<? extends Collection<? extends T>> mPrepends;

    @Nullable
    private Observable<? extends Collection<? extends T>> mAppends;

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

    public ObservableAdapterBuilder(@NonNull Binder<? extends T, ?> binder) {
        this(singletonMapper(binder));
    }

    public ObservableAdapterBuilder(@NonNull Mapper mapper) {
        mMapper = mapper;
    }

    /** Each emission of this observable overwrites the elements of the adapter. */
    @NonNull
    public ObservableAdapterBuilder<T> contents(@Nullable Observable<? extends Collection<? extends T>> contents) {
        mContents = contents;
        return this;
    }

    /** Each emission of this observable prepends to the elements of the adapter. */
    @NonNull
    public ObservableAdapterBuilder<T> prepends(@Nullable Observable<? extends Collection<? extends T>> prepends) {
        mPrepends = prepends;
        return this;
    }

    /** Each emission of this observable appends to the elements of the adapter. */
    @NonNull
    public ObservableAdapterBuilder<T> appends(@Nullable Observable<? extends Collection<? extends T>> appends) {
        mAppends = appends;
        return this;
    }

    /**
     * Defines the function for evaluating whether two objects have the same identity, for the purpose of determining
     * notifications.
     */
    @NonNull
    public ObservableAdapterBuilder<T> identityEquality(@Nullable EqualityFunction<? super T> identityEqualityFunction) {
        mIdentityEqualityFunction = identityEqualityFunction;
        return this;
    }

    /**
     * Sets whether the content diff engine should also detect item moves, as well as other changes.
     * <p>
     * This is {@code true} by default.
     */
    @NonNull
    public ObservableAdapterBuilder<T> detectMoves(boolean detectMoves) {
        mDetectMoves = detectMoves;
        return this;
    }

    /**
     * Defines the function for evaluating whether two objects have the same contents, for the purpose of determining
     * notifications.
     * <p>
     * By default, content equality is evaluated using {@link Object#equals(Object)}.
     */
    @NonNull
    public ObservableAdapterBuilder<T> contentEquality(@Nullable EqualityFunction<? super T> contentEqualityFunction) {
        mContentEqualityFunction = contentEqualityFunction;
        return this;
    }

    @NonNull
    public PowerAdapter build() {
        return new ObservableAdapter<>(mMapper, mContents, mPrepends, mAppends, mIdentityEqualityFunction,
                mContentEqualityFunction, mDetectMoves);
    }
}
