package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import lombok.NonNull;

import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.PowerAdapter.concat;
import static com.nextfaze.poweradapters.ViewFactories.asViewFactory;

/** Use {@link Condition}s instead. */
@Deprecated
public final class EmptyAdapterBuilder implements Decorator {

    @Nullable
    private Item mItem;

    @Nullable
    private Delegate mDelegate;

    private boolean mEnabled;

    @NonNull
    public EmptyAdapterBuilder resource(@LayoutRes int resource) {
        return view(asViewFactory(resource));
    }

    @NonNull
    public EmptyAdapterBuilder view(@NonNull ViewFactory viewFactory) {
        mItem = new Item(viewFactory, false);
        return this;
    }

    /**
     * Sets whether the empty item should be enabled in the list, allowing it to be clicked or not.
     * @param enabled {@code true} to make it enabled, otherwise {@code false} to make it disabled.
     * @see PowerAdapter#isEnabled(int)
     */
    @NonNull
    public EmptyAdapterBuilder enabled(boolean enabled) {
        mEnabled = enabled;
        return this;
    }

    /**
     * Sets the delegated used to determine the empty state. If {@code null}, a default implementation is provided that
     * checks the size of the wrapped adapter.
     */
    @NonNull
    public EmptyAdapterBuilder delegate(@Nullable Delegate delegate) {
        mDelegate = delegate;
        return this;
    }

    @CheckResult
    @NonNull
    public PowerAdapter build(@NonNull PowerAdapter adapter) {
        if (mItem == null) {
            return adapter;
        }
        final Delegate delegate = mDelegate != null ? mDelegate : new DefaultDelegate(adapter);
        return concat(adapter, asAdapter(mItem.withEnabled(mEnabled)).showOnlyWhile(delegate.mCondition));
    }

    @NonNull
    @Override
    public PowerAdapter decorate(@NonNull PowerAdapter adapter) {
        return build(adapter);
    }

    /** Invoked to determine when the empty item is shown. */
    @Deprecated
    public static abstract class Delegate {

        @NonNull
        private final AbstractCondition mCondition = new AbstractCondition() {
            @Override
            public boolean eval() {
                return isEmpty();
            }

            @Override
            protected void onFirstObserverRegistered() {
                Delegate.this.onFirstObserverRegistered();
            }

            @Override
            protected void onLastObserverUnregistered() {
                Delegate.this.onLastObserverUnregistered();
            }
        };

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
        public final void notifyEmptyChanged() {
            mCondition.notifyChanged();
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
