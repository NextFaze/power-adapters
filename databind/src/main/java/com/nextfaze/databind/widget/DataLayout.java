package com.nextfaze.databind.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nextfaze.databind.Data;
import com.nextfaze.databind.DataObserver;
import com.nextfaze.databind.ErrorFormatter;
import com.nextfaze.databind.ErrorObserver;
import com.nextfaze.databind.LoadingObserver;
import com.nextfaze.databind.R;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

/**
 * A container view that, when hooked up to an {@link Data}, will automatically show/hide internal views based
 * on loading/empty/error state of the adapter. Each {@link DataLayout} should contain at least a {@link AdapterView} of some
 * kind, an empty view, and a loading view. Each of these must be referenced by custom attributes for the auto layout
 * to be able to manage them.
 */
@Slf4j
@Accessors(prefix = "m")
public class DataLayout extends RelativeLayout {

    @NonNull
    private final DataObserver mDataSetObserver = new DataObserver() {
        @Override
        public void onChange() {
            updateViews();
        }

        @Override
        public void onInvalidated() {
        }
    };

    @NonNull
    private final LoadingObserver mLoadingObserver = new LoadingObserver() {
        @Override
        public void onLoadingChange() {
            mThrowable = null;
            updateErrorView();
            updateViews();
        }
    };

    @NonNull
    private final ErrorObserver mErrorObserver = new ErrorObserver() {
        @Override
        public void onError(@NonNull Throwable e) {
            mThrowable = e;
            updateErrorView();
            updateViews();
        }
    };

    private final int mAdapterViewId;
    private final int mEmptyViewId;
    private final int mLoadingViewId;
    private final int mErrorViewId;

    @Nullable
    private AdapterView<?> mAdapterView;

    @Nullable
    private View mEmptyView;

    @Nullable
    private View mLoadingView;

    @Nullable
    private View mErrorView;

    @Getter
    @Nullable
    private Data<?> mData;

    @Getter
    @Nullable
    private ErrorFormatter mErrorFormatter = ErrorFormatter.DEFAULT;

    @Nullable
    private Data<?> mRegisteredData;

    @Nullable
    private Throwable mThrowable;

    private boolean mAttachedToWindow;
    private boolean mShown;

    @SuppressWarnings("UnusedDeclaration")
    public DataLayout(Context context) {
        this(context, null);
    }

    public DataLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DataLayout, defStyle, 0);
        mAdapterViewId = a.getResourceId(R.styleable.DataLayout_adapterView, -1);
        mEmptyViewId = a.getResourceId(R.styleable.DataLayout_emptyView, -1);
        mLoadingViewId = a.getResourceId(R.styleable.DataLayout_loadingView, -1);
        mErrorViewId = a.getResourceId(R.styleable.DataLayout_errorView, -1);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        mAdapterView = (AdapterView<?>) findViewById(mAdapterViewId);
        mEmptyView = findViewById(mEmptyViewId);
        mLoadingView = findViewById(mLoadingViewId);
        mErrorView = findViewById(mErrorViewId);
        updateViews();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        updateShown();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
        updateShown();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        updateShown();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        updateShown();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        updateShown();
    }

    /**
     * Connects this view to a {@link Data} instance, so it can observe its loading/error/empty state and adjust child
     * view visibility accordingly.
     * @param data The data instance to observe, which may be {@link null} to cease observing anything.
     */
    public void setData(@Nullable Data<?> data) {
        if (data != mData) {
            mData = data;
            updateDataRegistration();
            updateViews();
            // We may already be showing, so notify new data.
            if (data != null) {
                if (mShown) {
                    data.notifyShown();
                } else {
                    data.notifyHidden();
                }
            }
        }
    }

    public void setErrorFormatter(@Nullable ErrorFormatter errorFormatter) {
        if (errorFormatter != mErrorFormatter) {
            mErrorFormatter = errorFormatter;
            updateErrorView();
        }
    }

    private void updateShown() {
        boolean shown = isThisViewShown();
        if (shown != mShown) {
            mShown = shown;
            updateDataRegistration();
            updateViews();
            if (shown) {
                if (mData != null) {
                    mData.notifyShown();
                }
            } else {
                if (mData != null) {
                    mData.notifyHidden();
                }
            }
        }
    }

    private boolean isThisViewShown() {
        // TODO: Can we use View.isShown()?
        return mAttachedToWindow && getWindowVisibility() == VISIBLE;
    }

    private void updateDataRegistration() {
        if (mShown) {
            changeRegisteredData(mData);
        } else {
            changeRegisteredData(null);
        }
    }

    private void changeRegisteredData(@Nullable Data<?> data) {
        if (data != mRegisteredData) {
            if (mRegisteredData != null) {
                mRegisteredData.unregisterDataObserver(mDataSetObserver);
                mRegisteredData.unregisterLoadingObserver(mLoadingObserver);
                mRegisteredData.unregisterErrorObserver(mErrorObserver);
            }
            mRegisteredData = data;
            if (mRegisteredData != null) {
                mRegisteredData.registerDataObserver(mDataSetObserver);
                mRegisteredData.registerLoadingObserver(mLoadingObserver);
                mRegisteredData.registerErrorObserver(mErrorObserver);
            }
        }
    }

    private void updateViews() {
        if (mData == null) {
            // No data, show empty.
            hide(mAdapterView);
            show(mEmptyView);
            hide(mLoadingView);
            hide(mErrorView);
        } else {
            if (mData.isEmpty()) {
                if (mData.isLoading()) {
                    // Empty, but loading, so show loading.
                    hide(mAdapterView);
                    hide(mEmptyView);
                    show(mLoadingView);
                    hide(mErrorView);
                } else {
                    if (mThrowable == null) {
                        // Empty, not loading, no error, so show empty.
                        hide(mAdapterView);
                        show(mEmptyView);
                        hide(mLoadingView);
                        hide(mErrorView);
                    } else {
                        // Empty, not loading, but has an error, so show error.
                        hide(mAdapterView);
                        hide(mEmptyView);
                        hide(mLoadingView);
                        show(mErrorView);
                    }
                }
            } else {
                // Not empty, show adapter view.
                show(mAdapterView);
                hide(mEmptyView);
                hide(mLoadingView);
                hide(mErrorView);
            }
        }
    }

    private void updateErrorView() {
        if (mErrorView instanceof TextView) {
            TextView errorView = (TextView) mErrorView;
            errorView.setText(getErrorMessage());
        }
    }

    @Nullable
    private String getErrorMessage() {
        if (mErrorFormatter == null) {
            return null;
        }
        if (mThrowable == null) {
            return null;
        }
        return mErrorFormatter.format(getContext(), mThrowable);
    }

    private static void show(@Nullable View v) {
        if (v != null) {
            v.setVisibility(VISIBLE);
        }
    }

    private static void hide(@Nullable View v) {
        if (v != null) {
            v.setVisibility(GONE);
        }
    }
}
