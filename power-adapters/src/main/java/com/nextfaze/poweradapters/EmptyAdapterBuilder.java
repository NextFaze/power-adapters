package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ListAdapter;
import lombok.NonNull;

import static com.nextfaze.poweradapters.PowerAdapters.concat;
import static com.nextfaze.poweradapters.ViewFactories.viewFactoryForResource;
import static com.nextfaze.poweradapters.ViewFactories.viewFactoryForView;

public final class EmptyAdapterBuilder implements Decorator {

    @Nullable
    private Item mItem;

    private boolean mEnabled;

    @NonNull
    public EmptyAdapterBuilder resource(@LayoutRes int resource) {
        return view(viewFactoryForResource(resource));
    }

    @NonNull
    public EmptyAdapterBuilder view(@NonNull View view) {
        return view(viewFactoryForView(view));
    }

    @NonNull
    public EmptyAdapterBuilder view(@NonNull ViewFactory viewFactory) {
        mItem = new Item(viewFactory, false);
        return this;
    }

    /**
     * Sets whether the empty item should be enabled in the list, allowing it to be clicked or not.
     * @param enabled {@code true} to make it enabled, otherwise {@code false} to make it disabled.
     * @see ListAdapter#isEnabled(int)
     */
    @NonNull
    public EmptyAdapterBuilder enabled(boolean enabled) {
        mEnabled = enabled;
        return this;
    }

    @CheckResult
    @NonNull
    public PowerAdapter build(@NonNull PowerAdapter adapter) {
        return build(adapter, new DefaultDelegate(adapter));
    }

    @CheckResult
    @NonNull
    public PowerAdapter build(@NonNull PowerAdapter adapter, @NonNull Delegate delegate) {
        if (mItem == null) {
            return adapter;
        }
        return concat(adapter, new EmptyAdapter(delegate, mItem.withEnabled(mEnabled)));
    }

    @NonNull
    @Override
    public PowerAdapter decorate(@NonNull PowerAdapter adapter) {
        return build(adapter);
    }

    /** Invoked by {@link EmptyAdapter} to determine when the empty item is shown. */
    public static abstract class Delegate {

        @Nullable
        private EmptyAdapter mAdapter;

        /**
         * Determines if the empty item should currently be shown.
         * Invoke {@link #notifyEmptyChanged()} to inform the owning adapter if the empty state has changed.
         * @return {@code true} if the empty item should be shown right now, otherwise {@code false}.
         * @see #notifyEmptyChanged()
         */
        protected abstract boolean isEmpty();

        /**
         * @see PowerAdapterWrapper#onFirstObserverRegistered()
         */
        @UiThread
        protected void onFirstObserverRegistered() {
        }

        /**
         * @see PowerAdapterWrapper#onLastObserverUnregistered()
         */
        @UiThread
        protected void onLastObserverUnregistered() {
        }

        /** Must be called when the value of {@link #isEmpty()} changes. */
        @UiThread
        protected final void notifyEmptyChanged() {
            if (mAdapter != null) {
                mAdapter.updateVisible();
            }
        }

        void setAdapter(@Nullable EmptyAdapter adapter) {
            mAdapter = adapter;
        }
    }

    /** Empty state is determined by wrapped adapter size. */
    private static class DefaultDelegate extends Delegate {

        @NonNull
        private final PowerAdapter mAdapter;

        @NonNull
        private final DataObserver mDataObserver = new SimpleDataObserver() {
            @Override
            public void onChanged() {
                notifyEmptyChanged();
            }
        };

        DefaultDelegate(@NonNull PowerAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        protected boolean isEmpty() {
            return mAdapter.getItemCount() == 0;
        }

        @Override
        protected void onFirstObserverRegistered() {
            super.onFirstObserverRegistered();
            mAdapter.registerDataObserver(mDataObserver);
        }

        @Override
        protected void onLastObserverUnregistered() {
            super.onLastObserverUnregistered();
            mAdapter.unregisterDataObserver(mDataObserver);
        }
    }
}
