package com.nextfaze.poweradapters.data;

import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.binding.BindingAdapter;
import com.nextfaze.poweradapters.binding.Mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

public final class DataBindingAdapter<T> extends BindingAdapter<T> {

    @NonNull
    private final DataObserver mDataObserver = new DataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    };

    @NonNull
    private final Data<? extends T> mData;

    public DataBindingAdapter(@NonNull Binder<? super T, ?> binder, @NonNull Data<? extends T> data) {
        this(singletonMapper(binder), data);
    }

    public DataBindingAdapter(@NonNull Mapper<? super T> mapper, @NonNull Data<? extends T> data) {
        super(mapper);
        mData = checkNotNull(data, "data");
    }

    /** @deprecated Use {@link DataBindingAdapter#DataBindingAdapter(Binder, Data)} instead. */
    @Deprecated
    public DataBindingAdapter(@NonNull Data<? extends T> data, @NonNull Binder<? super T, ?> binder) {
        this(singletonMapper(binder), data);
    }

    /** @deprecated Use {@link DataBindingAdapter#DataBindingAdapter(Mapper, Data)} instead. */
    @Deprecated
    public DataBindingAdapter(@NonNull Data<? extends T> data, @NonNull Mapper<? super T> mapper) {
        this(mapper, data);
    }

    @NonNull
    @Override
    protected T getItem(int position) {
        return mData.get(position, Data.FLAG_PRESENTATION);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mData.registerDataObserver(mDataObserver);
    }

    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mData.unregisterDataObserver(mDataObserver);
    }
}
