package com.nextfaze.asyncdata;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;

public class ListAdapterWrapper extends BaseAdapter implements DisposableListAdapter {

    private static final Logger log = LoggerFactory.getLogger(ListAdapterWrapper.class);

    @NonNull
    protected final ListAdapter mAdapter;

    @NonNull
    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            notifyDataSetInvalidated();
        }
    };

    private final boolean mTakeOwnership;

    /**
     * Create a list adapter wrapper, taking ownership of the wrapped adapter.
     * @param adapter The adapter to be wrapped.
     * @see #ListAdapterWrapper(ListAdapter, boolean)
     */
    public ListAdapterWrapper(@NonNull ListAdapter adapter) {
        this(adapter, true);
    }

    /**
     * Create a list adapter wrapper, optionally taking ownership of the wrapped adapter.
     * @param adapter The adapter to be wrapped.
     * @param takeOwnership If {@code true}, this adapter assumes ownership of the wrapped adapter and must dispose of
     * it.
     */
    public ListAdapterWrapper(@NonNull ListAdapter adapter, boolean takeOwnership) {
        mAdapter = adapter;
        mTakeOwnership = takeOwnership;
        mAdapter.registerDataSetObserver(mDataSetObserver);
    }

    @Override
    public void dispose() {
        mAdapter.unregisterDataSetObserver(mDataSetObserver);
        if (mTakeOwnership) {
            close(mAdapter);
        }
    }

    private static void close(@Nullable ListAdapter adapter) {
        if (adapter instanceof DisposableListAdapter) {
            ((DisposableListAdapter) adapter).dispose();
        }
        if (adapter instanceof Closeable) {
            try {
                ((Closeable) adapter).close();
            } catch (IOException e) {
                log.warn("Error closing wrapped adapter", e);
            }
        }
    }

    @Override
    public int getCount() {
        return mAdapter.getCount();
    }

    @Override
    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return mAdapter.isEnabled(position);
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return mAdapter.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(position);
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mAdapter.getView(position, convertView, parent);
    }

    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(position);
    }

    /** Subclasses must always return the sum of the super call and any additional view types they provide. */
    @Override
    public int getViewTypeCount() {
        return mAdapter.getViewTypeCount();
    }
}
