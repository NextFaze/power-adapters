package com.nextfaze.databind.widget;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nextfaze.databind.Data;
import com.nextfaze.databind.ErrorFormatter;
import com.nextfaze.databind.R;
import com.nextfaze.databind.util.DataWatcher;
import lombok.NonNull;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.SystemClock.elapsedRealtime;

/**
 * A container view that, when hooked up to a {@link Data} instance, will automatically show/hide child views based
 * on loading/empty/error state of the data.
 * <p/>Each {@link DataLayout} should contain an {@link AdapterView} of some
 * kind (although this is not mandatory, it can be any view), an empty view, a loading view, and an error view.
 * <p/>Each of these must be referenced by custom attributes for the layout to be able to manage them.
 * @author Ben Tilbrook
 */
@Accessors(prefix = "m")
public class DataLayout extends RelativeLayout {

    private static final String KEY_SUPER_STATE = "superState";
    private static final String KEY_ERROR_MESSAGE = "errorMessage";

    @NonNull
    private final DataWatcher mDataWatcher = new DataWatcher() {
        @Override
        public void onDataChange() {
            updateViews();
        }

        @Override
        public void onDataLoadingChange() {
            Data<?> data = getData();
            if (data != null && data.isLoading()) {
                // Loading has started again, so clear error status.
                mErrorMessage = null;
            }
            updateErrorView();
            updateViews();
        }

        @Override
        public void onDataError(@NonNull Throwable e) {
            mErrorMessage = formatErrorMessage(e);
            updateErrorView();
            updateViews();
        }
    };

    /** Child view ID for displaying contents of {@link #mData}. */
    @IdRes
    private final int mContentViewId;

    /** Child view ID shown while {@link #mData} is empty. */
    @IdRes
    private final int mEmptyViewId;

    /** Child view ID shown while {@link #mData} is loading. */
    @IdRes
    private final int mLoadingViewId;

    /** Child view ID shown when {@link #mData} reports an error. */
    @IdRes
    private final int mErrorViewId;

    /** Child view for displaying contents of {@link #mData}. */
    @Nullable
    private View mContentView;

    /** Child view shown while {@link #mData} is empty. */
    @Nullable
    private View mEmptyView;

    /** Child view shown while {@link #mData} is loading. */
    @Nullable
    private View mLoadingView;

    /** Child view shown when {@link #mData} reports an error. */
    @Nullable
    private View mErrorView;

    /** The data instance used to determine child view visibility. */
    @Nullable
    private Data<?> mData;

    /** The current error message, if any. */
    @Nullable
    private String mErrorMessage;

    /** Formats error messages to be displayed in the error view. */
    @Nullable
    private ErrorFormatter mErrorFormatter = ErrorFormatter.DEFAULT;

    @NonNull
    private VisibilityPolicy mVisibilityPolicy = VisibilityPolicy.DEFAULT;

    /** Indicates this view is attached to the window. */
    private boolean mAttachedToWindow;

    /** Indicates this view is visible to the user. */
    private boolean mShown;

    /** Track when this view became shown. */
    private long mShownTime;

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

        LayoutTransition transition = new LayoutTransition();
        transition.setStartDelay(LayoutTransition.APPEARING, 0);
        transition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
        transition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
        transition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        setLayoutTransition(transition);
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
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            Parcelable superState = bundle.getParcelable(KEY_SUPER_STATE);
            super.onRestoreInstanceState(superState);
            mErrorMessage = bundle.getString(KEY_ERROR_MESSAGE);
        }
        updateViews();
        updateErrorView();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState());
        bundle.putString(KEY_ERROR_MESSAGE, mErrorMessage);
        return bundle;
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
        if (visibility == VISIBLE) {
            updateShown();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (mInflated && visibility == VISIBLE) {
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
     * @param newData The data instance to observe, which may be {@code null} to cease observing anything.
     */
    public final void setData(@Nullable Data<?> newData) {
        Data<?> oldData = mData;
        mDataWatcher.setData(newData);
        if (newData != oldData) {
            mData = newData;
            updateViews();
            // Old data needs to be notified it's no longer shown.
            if (oldData != null) {
                oldData.notifyHidden();
            }
            // We may already be showing, so notify new data.
            if (newData != null) {
                if (mShown) {
                    newData.notifyShown();
                } else {
                    newData.notifyHidden();
                }
            }
        }
    }

    /** Get the data instance used to control child view visibility. */
    @Nullable
    public final Data<?> getData() {
        return mData;
    }

    /** Set the error formatter used to present exceptions in the error child view. */
    public final void setErrorFormatter(@Nullable ErrorFormatter errorFormatter) {
        if (errorFormatter != mErrorFormatter) {
            mErrorFormatter = errorFormatter;
            updateErrorView();
        }
    }

    /** Get the error formatter used to present exceptions in the error child view. */
    @Nullable
    public final ErrorFormatter getErrorFormatter() {
        return mErrorFormatter;
    }

    @NonNull
    public final VisibilityPolicy getVisibilityPolicy() {
        return mVisibilityPolicy;
    }

    public final void setVisibilityPolicy(@NonNull VisibilityPolicy visibilityPolicy) {
        if (visibilityPolicy != mVisibilityPolicy) {
            mVisibilityPolicy = visibilityPolicy;
            updateViews();
        }
    }

    @Nullable
    public final View getContentView() {
        return mContentView;
    }

    public final void setContentView(@Nullable View contentView) {
        if (contentView != mContentView) {
            mContentView = contentView;
            updateViews();
        }
    }

    @Nullable
    public final View getEmptyView() {
        return mEmptyView;
    }

    public final void setEmptyView(@Nullable View emptyView) {
        if (emptyView != mEmptyView) {
            mEmptyView = emptyView;
            updateViews();
        }
    }

    @Nullable
    public final View getLoadingView() {
        return mLoadingView;
    }

    public final void setLoadingView(@Nullable View loadingView) {
        if (loadingView != mLoadingView) {
            mLoadingView = loadingView;
            updateViews();
        }
    }

    @Nullable
    public final View getErrorView() {
        return mErrorView;
    }

    public final void setErrorView(@Nullable View errorView) {
        if (errorView != mErrorView) {
            mErrorView = errorView;
            updateViews();
        }
    }

    private void updateShown() {
        boolean shown = isThisViewShown();
        mDataWatcher.setEnabled(shown);
        if (shown != mShown) {
            mShown = shown;
            if (shown) {
                mShownTime = elapsedRealtime();
            } else {
                mShownTime = 0;
            }
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

    /** Returns if this view is currently considered "shown" based on various attributes. */
    private boolean isThisViewShown() {
        return mAttachedToWindow && getWindowVisibility() == VISIBLE && getVisibility() == VISIBLE && isShown();
    }

    /** Returns whether now is an appropriate time to perform animations. */
    private boolean shouldAnimate() {
        Display display = currentDisplay();
        if (display == null) {
            return false;
        }
        if (mShownTime <= 0) {
            return false;
        }
        long threshold = (long) (1000 / display.getRefreshRate());
        long millisShown = elapsedRealtime() - mShownTime;
        return millisShown > threshold;
    }

    /** Get a {@link Display} object suitable for checking the refresh rate. */
    @Nullable
    private Display currentDisplay() {
        if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
            return getDisplay();
        } else {
            return ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        }
    }

    private void updateViews() {
        onlyShow(viewToBeShown(), shouldAnimate());
    }

    private void onlyShow(@Nullable View v, boolean animated) {
        setShown(mContentView, v == mContentView, animated);
        setShown(mLoadingView, v == mLoadingView, animated);
        setShown(mEmptyView, v == mEmptyView, animated);
        setShown(mErrorView, v == mErrorView, animated);
    }

    @Nullable
    private View viewToBeShown() {
        if (mContentView != null && mVisibilityPolicy.shouldShow(this, mContentView)) {
            return mContentView;
        }
        if (mLoadingView != null && mVisibilityPolicy.shouldShow(this, mLoadingView)) {
            return mLoadingView;
        }
        if (mErrorView != null && mVisibilityPolicy.shouldShow(this, mErrorView)) {
            return mErrorView;
        }
        if (mEmptyView != null && mVisibilityPolicy.shouldShow(this, mEmptyView)) {
            return mEmptyView;
        }
        return null;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean shouldShow(@NonNull View v) {
        if (v == mContentView) {
            return shouldShowContents();
        }
        if (v == mLoadingView) {
            return shouldShowLoading();
        }
        if (v == mEmptyView) {
            return shouldShowEmpty();
        }
        if (v == mErrorView) {
            return shouldShowError();
        }
        return false;
    }

    private boolean shouldShowContents() {
        return mData != null && !mData.isEmpty();
    }

    private boolean shouldShowLoading() {
        return mData != null && mData.isLoading();
    }

    private boolean shouldShowError() {
        return mErrorMessage != null;
    }

    private boolean shouldShowEmpty() {
        return mData == null || mData.isEmpty();
    }

    private void updateErrorView() {
        if (mErrorView instanceof TextView) {
            TextView errorView = (TextView) mErrorView;
            errorView.setText(mErrorMessage);
        }
    }

    @Nullable
    private String formatErrorMessage(@NonNull Throwable e) {
        if (mErrorFormatter == null) {
            return null;
        }
        return mErrorFormatter.format(getContext(), e);
    }

    private void setShown(@Nullable View v, boolean shown, boolean animated) {
        if (shown) {
            show(v, animated);
        } else {
            hide(v, animated);
        }
    }

    private boolean show(@Nullable View v, boolean animated) {
        if (v == null) {
            return false;
        }
        v.setVisibility(VISIBLE);
        return true;
    }

    private boolean hide(@Nullable View v, boolean animated) {
        if (v == null) {
            return false;
        }
        v.setVisibility(GONE);
        return true;
    }

    /** Allows control of visibility of each {@link DataLayout} component. */
    public interface VisibilityPolicy {

        /**
         * @see DataLayout#shouldShow(View)
         */
        VisibilityPolicy DEFAULT = new VisibilityPolicy() {
            @Override
            public boolean shouldShow(@NonNull DataLayout dataLayout, @NonNull View v) {
                return dataLayout.shouldShow(v);
            }
        };

        /**
         * Called to determine whether or not to show the specified view. Callbacks are made to for each of the
         * following views in order:
         * <ol>
         * <li>Content</li>
         * <li>Loading</li>
         * <li>Error</li>
         * <li>Empty</li>
         * </ol>
         * The view of the first callback to return {@code true} will be made visible.
         * @param dataLayout The data layout view.
         * @param v The child component view being checked for visibility.
         * @return {@code true} makes the view visible, {@code} makes it invisible.
         */
        boolean shouldShow(@NonNull DataLayout dataLayout, @NonNull View v);
    }
}
