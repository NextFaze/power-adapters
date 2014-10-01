package com.nextfaze.databind.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nextfaze.databind.Data;
import com.nextfaze.databind.ErrorFormatter;
import com.nextfaze.databind.R;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

/**
 * A container view that, when hooked up to a {@link Data} instance, will automatically show/hide internal views based
 * on loading/empty/error state of the data. Each {@link DataLayout} should contain an {@link AdapterView} of some
 * kind (although this is not mandatory), an empty view, a loading view, and an error view. Each of these must be
 * referenced by custom attributes for the layout to be able to manage them.
 * @author Ben Tilbrook
 */
@Slf4j
@Accessors(prefix = "m")
public class DataLayout extends RelativeLayout {

    @NonNull
    private final DataWatcher mDataWatcher = new DataWatcher() {
        @Override
        public void onChange() {
            updateViews();
        }

        @Override
        public void onLoadingChange() {
            mThrowable = null;
            updateErrorView();
            updateViews();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            mThrowable = e;
            updateErrorView();
            updateViews();
        }
    };

    private final int mContentViewId;
    private final int mEmptyViewId;
    private final int mLoadingViewId;
    private final int mErrorViewId;

    @Nullable
    private View mContentView;

    @Nullable
    private View mEmptyView;

    @Nullable
    private View mLoadingView;

    @Nullable
    private View mErrorView;

    @Getter
    @Nullable
    private Data<?> mData;

    @Nullable
    private Throwable mThrowable;

    @Getter
    @Nullable
    private ErrorFormatter mErrorFormatter = ErrorFormatter.DEFAULT;

    private boolean mAttachedToWindow;
    private boolean mShown;

    /** Used to work around NPE caused by {@link #onVisibilityChanged(View, int)} self call in super class. */
    private boolean mInflated;

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
        mContentViewId = a.getResourceId(R.styleable.DataLayout_contentView, -1);
        mEmptyViewId = a.getResourceId(R.styleable.DataLayout_emptyView, -1);
        mLoadingViewId = a.getResourceId(R.styleable.DataLayout_loadingView, -1);
        mErrorViewId = a.getResourceId(R.styleable.DataLayout_errorView, -1);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        mInflated = true;
        mContentView = findViewById(mContentViewId);
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
        if (mInflated) {
            updateShown();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mInflated) {
            updateShown();
        }
    }

    /**
     * Connects this view to a {@link Data} instance, so it can observe its loading/error/empty state and adjust child
     * view visibility accordingly.
     * @param data The data instance to observe, which may be {@link null} to cease observing anything.
     */
    public void setData(@Nullable Data<?> data) {
        mDataWatcher.setData(data);
        if (data != mData) {
            mData = data;
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
        mDataWatcher.setShown(shown);
        if (shown != mShown) {
            mShown = shown;
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
        return mAttachedToWindow && getWindowVisibility() == VISIBLE && getVisibility() == VISIBLE;
    }

    private void updateViews() {
        if (mData == null) {
            // No data, show empty.
            hide(mContentView);
            show(mEmptyView);
            hide(mLoadingView);
            hide(mErrorView);
        } else {
            if (mData.isEmpty()) {
                if (mData.isLoading()) {
                    // Empty, but loading, so show loading.
                    hide(mContentView);
                    hide(mEmptyView);
                    show(mLoadingView);
                    hide(mErrorView);
                } else {
                    if (mThrowable == null) {
                        // Empty, not loading, no error, so show empty.
                        hide(mContentView);
                        show(mEmptyView);
                        hide(mLoadingView);
                        hide(mErrorView);
                    } else {
                        // Empty, not loading, but has an error, so show error.
                        hide(mContentView);
                        hide(mEmptyView);
                        hide(mLoadingView);
                        show(mErrorView);
                    }
                }
            } else {
                // Not empty, show adapter view.
                show(mContentView);
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
