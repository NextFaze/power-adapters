package com.nextfaze.asyncdata.internal;

import android.support.annotation.Nullable;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.DataObserver;
import com.nextfaze.asyncdata.ErrorObserver;
import com.nextfaze.asyncdata.LoadingObserver;
import com.nextfaze.asyncdata.SimpleDataObserver;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Convenience class for observing a {@link Data} instance, and ensuring observers are correctly
 * registered/unregistered.
 */
@Accessors(prefix = "m")
public abstract class DataWatcher {

    private static final Logger log = LoggerFactory.getLogger(DataWatcher.class);

    @NonNull
    private final DataObserver mDataObserver = new SimpleDataObserver() {
        @Override
        public void onChange() {
            dispatchDataChange();
        }
    };

    @NonNull
    private final LoadingObserver mLoadingObserver = new LoadingObserver() {
        @Override
        public void onLoadingChange() {
            dispatchDataLoadingChange();
        }
    };

    @NonNull
    private final ErrorObserver mErrorObserver = new ErrorObserver() {
        @Override
        public void onError(@NonNull Throwable e) {
            dispatchDataError(e);
        }
    };

    @Nullable
    private Data<?> mData;

    @NonNull
    private final Set<Data<?>> mDatas = new HashSet<>();

    /** Keep track of the data instance registered against, so we can unregister easily later. */
    @Nullable
    private Data<?> mDataRegistered;

    @NonNull
    private final Map<Data<?>, Boolean> mRegistered = new HashMap<>();

    private boolean mEnabled;

    /**
     * Assigns a data instance, and updates the observer registration state based on {@link #isEnabled()}.
     * @param data The data instance to observe, possibly {@code null}.
     * @see #setEnabled(boolean)
     */
    public final void setData(@Nullable Data<?> data) {
        if (data != mData) {
            mData = data;
            updateRegistration();
        }
    }

    public final void setDatas(@NonNull Data<?>... datas) {
        setDatas(asList(datas));
    }

    public final void setDatas(@NonNull Iterable<? extends Data<?>> datas) {
        mDatas.clear();

        for (Data<?> data : datas) {
            mDatas.add(data);
            // Add entry for each new data instance in our registration map.
            if (!mRegistered.containsKey(data)) {
                mRegistered.put(data, false);
            }
        }

        // Remove and unregister any entries in our registration map that aren't in the new set.
        Iterator<Map.Entry<Data<?>, Boolean>> it = mRegistered.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Data<?>, Boolean> entry = it.next();
            Data<?> data = entry.getKey();
            Boolean registered = entry.getValue();
            if (!mDatas.contains(data) && registered) {
                data.unregisterDataObserver(mDataObserver);
                data.unregisterLoadingObserver(mLoadingObserver);
                data.unregisterErrorObserver(mErrorObserver);
                it.remove();
            }
        }

        updateRegistration();
    }

    /** Return the current data instance being observed, possibly {@code null}. */
    @Nullable
    public final Data<?> getData() {
        return mData;
    }

    /**
     * Flags this watcher as active or inactive, which causes registration against the assigned {@link Data}. This can
     * be called many times without triggering duplicate registrations, and can be called with or without {@link
     * #setData(Data)} having been set beforehand.
     * @param enabled If {@code true}, ensures observers are registered. If {@code false}, ensures observers are
     * unregistered.
     */
    public final void setEnabled(boolean enabled) {
        if (enabled != mEnabled) {
            mEnabled = enabled;
            updateRegistration();
        }
    }

    /**
     * Indicates if this instance is currently active.
     * @return If {@code true}, observers are registered. If {@code false}, observers are not registered.
     */
    public final boolean isEnabled() {
        return mEnabled;
    }

    private void updateRegistration() {
        for (Map.Entry<Data<?>, Boolean> entry : mRegistered.entrySet()) {
            Data<?> data = entry.getKey();
            Boolean registered = entry.getValue();
            boolean shouldRegister = mEnabled && mDatas.contains(data);
            if (registered && !shouldRegister) {
                data.unregisterDataObserver(mDataObserver);
                data.unregisterLoadingObserver(mLoadingObserver);
                data.unregisterErrorObserver(mErrorObserver);
                entry.setValue(false);
            }
            if (!registered && shouldRegister) {
                data.registerDataObserver(mDataObserver);
                data.registerLoadingObserver(mLoadingObserver);
                data.registerErrorObserver(mErrorObserver);
                entry.setValue(true);
            }
        }
    }

    private void dispatchDataChange() {
        try {
            onDataChange();
        } catch (Throwable e) {
            log.error("Error dispatching data change callback", e);
        }
    }

    private void dispatchDataLoadingChange() {
        try {
            onDataLoadingChange();
        } catch (Throwable e) {
            log.error("Error dispatching loading change callback", e);
        }
    }

    private void dispatchDataError(@NonNull Throwable e) {
        try {
            onDataError(e);
        } catch (Throwable t) {
            log.error("Error dispatching error callback", t);
        }
    }

    /** Called when the data changes. */
    protected void onDataChange() {
    }

    /** Called when the data loading state changes. */
    protected void onDataLoadingChange() {
    }

    /** Called when the data dispatches an error. */
    protected void onDataError(@NonNull Throwable e) {
    }
}
