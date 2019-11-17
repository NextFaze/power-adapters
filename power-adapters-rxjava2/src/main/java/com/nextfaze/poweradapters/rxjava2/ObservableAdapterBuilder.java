package com.nextfaze.poweradapters.rxjava2;

import android.view.View;

import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.binding.Mapper;

import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Observable;

import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

/**
 * Builder class for constructing {@link PowerAdapter}s out of observable data sources.
 * @param <T> The element type of the built adapter.
 */
public final class ObservableAdapterBuilder<T> {

    @NonNull
    private final Mapper<? super T> mMapper;

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

    public ObservableAdapterBuilder(@NonNull Binder<? super T, ? extends View> binder) {
        this(singletonMapper(binder));
    }

    public ObservableAdapterBuilder(@NonNull Mapper<? super T> mapper) {
        mMapper = checkNotNull(mapper, "mapper");
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
