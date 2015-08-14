package com.nextfaze.asyncdata.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.AnimatorRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.ErrorFormatter;
import com.nextfaze.asyncdata.R;
import com.nextfaze.asyncdata.util.DataWatcher;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.SystemClock.elapsedRealtime;

/**
 * A container view that, when hooked up to a {@link Data} instance, will automatically show/hide child component views
 * based on the loading/empty/error state of the data.
 * <p/>Each {@link DataLayout} should usually contain the following components as child views:
 *
 * <h2>Content View</h2>
 * An {@link AdapterView} of some kind, or a {@code RecyclerView} (although this is not mandatory, it can be any view).
 * This view is assigned by specifying the {@link R.styleable#DataLayout_contentView} attribute.
 *
 * <h2>Empty View</h2>
 * An empty view, which will be shown while the {@link Data} is empty. This view is assigned by specifying the
 * {@link R.styleable#DataLayout_emptyView} attribute.
 *
 * <h2>Loading View</h2>
 * A loading view, which will be shown while the {@link Data} is empty and loading. This view is assigned by specifying the
 * {@link R.styleable#DataLayout_loadingView} attribute.
 *
 * <h2>Error View</h2>
 * An error view, which will be shown if the {@link Data} emits an error. This view is assigned by specifying the
 * {@link R.styleable#DataLayout_errorView} attribute.
 */
@Accessors(prefix = "m")
public class DataLayout extends RelativeLayout {

    private static final Logger log = LoggerFactory.getLogger(DataLayout.class);

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
    private CharSequence mErrorMessage;

    /** Formats error messages to be displayed in the error view. */
    @Nullable
    private ErrorFormatter mErrorFormatter = ErrorFormatter.DEFAULT;

    @NonNull
    private VisibilityPolicy mVisibilityPolicy = VisibilityPolicy.DEFAULT;

    /** Animator used to show views. */
    @Nullable
    private Animator mAnimatorIn;

    /** Animator used to hide views. */
    @Nullable
    private Animator mAnimatorOut;

    @Nullable
    private View mVisibleView;

    /** Indicates this view is attached to the window. */
    private boolean mAttachedToWindow;

    /** Indicates this view is visible to the user. */
    private boolean mShown;

    /** Track when this view became shown. */
    private long mShownTime;

    /** Used to work around NPE caused by {@link #onVisibilityChanged(View, int)} self call in super class. */
    private boolean mInflated;

    /** Indicates animations will run as inner views show and hide. */
    private boolean mAnimationEnabled = true;

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
        mAnimatorIn = loadAnimator(context, a, R.styleable.DataLayout_animatorIn, R.animator.data_layout_default_in);
        mAnimatorOut = loadAnimator(context, a, R.styleable.DataLayout_animatorOut, R.animator.data_layout_default_out);
        mAnimationEnabled = a.getBoolean(R.styleable.DataLayout_animationsEnabled, mAnimationEnabled);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInflated = true;
        mContentView = findViewById(mContentViewId);
        if (mContentView != null) {
            mContentView.setVisibility(INVISIBLE);
        }
        mEmptyView = findViewById(mEmptyViewId);
        if (mEmptyView != null) {
            mEmptyView.setVisibility(INVISIBLE);
        }
        mLoadingView = findViewById(mLoadingViewId);
        if (mLoadingView != null) {
            mLoadingView.setVisibility(INVISIBLE);
        }
        mErrorView = findViewById(mErrorViewId);
        if (mErrorView != null) {
            mErrorView.setVisibility(INVISIBLE);
        }
        updateViews();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            Parcelable superState = bundle.getParcelable(KEY_SUPER_STATE);
            super.onRestoreInstanceState(superState);
            mErrorMessage = bundle.getCharSequence(KEY_ERROR_MESSAGE);
        }
        updateViews();
        updateErrorView();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState());
        bundle.putCharSequence(KEY_ERROR_MESSAGE, mErrorMessage);
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

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateShown();
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

    /** Set the error formatter used to present {@link Throwable} objects in the error child view. */
    public final void setErrorFormatter(@Nullable ErrorFormatter errorFormatter) {
        if (errorFormatter != mErrorFormatter) {
            mErrorFormatter = errorFormatter;
            updateErrorView();
        }
    }

    /** Get the error formatter used to present {@link Throwable} objects in the error child view. */
    @Nullable
    public final ErrorFormatter getErrorFormatter() {
        return mErrorFormatter;
    }

    /** Get the policy used to determine the visibility of each child component view. */
    @NonNull
    public final VisibilityPolicy getVisibilityPolicy() {
        return mVisibilityPolicy;
    }

    /** Set the policy used to determine the visibility of each child component view. */
    public final void setVisibilityPolicy(@NonNull VisibilityPolicy visibilityPolicy) {
        if (visibilityPolicy != mVisibilityPolicy) {
            mVisibilityPolicy = visibilityPolicy;
            updateViews();
        }
    }

    /** Indicates whether animations will run on child views. */
    public final boolean isAnimationEnabled() {
        return mAnimationEnabled;
    }

    /** Controls whether animations will run on child views. {@code true} by default. */
    public final void setAnimationEnabled(boolean animationEnabled) {
        mAnimationEnabled = animationEnabled;
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
        return mAttachedToWindow && getWindowVisibility() == VISIBLE && getVisibility() == VISIBLE && isShown() &&
                isEnabled();
    }

    /** Returns whether now is an appropriate time to perform animations. */
    private boolean shouldAnimate() {
        if (!mAnimationEnabled) {
            return false;
        }
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
        changeView(viewToBeShown(), shouldAnimate());
    }

    private void changeView(@Nullable View newView, boolean animated) {
        View oldView = mVisibleView;
        if (newView != oldView) {
            mVisibleView = newView;
            if (oldView != null) {
                animateOut(oldView, !animated);
            }
            if (newView != null) {
                animateIn(newView, !animated);
            }
        }
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
    private CharSequence formatErrorMessage(@NonNull Throwable e) {
        if (mErrorFormatter == null) {
            return null;
        }
        return mErrorFormatter.format(getContext(), e);
    }

    //region Animation

    private void animateIn(@NonNull final View v, boolean immediately) {
        v.setVisibility(VISIBLE);
        Animator animator = createAnimatorIn();
        if (animator != null) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onAnimateInEnd(v);
                }
            });
            animate(v, animator, immediately);
        }
    }

    private static void onAnimateInEnd(@NonNull View v) {
        setAnimator(v, null);
    }

    private void animateOut(@NonNull final View v, boolean immediately) {
        Animator animator = createAnimatorOut();
        if (animator != null) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onAnimateOutEnd(animation, v);
                }
            });
            animate(v, animator, immediately);
        }
    }

    private static void onAnimateOutEnd(Animator animation, @NonNull View v) {
        // Check the animator here to prevent canceled animations from unintentionally hiding the view.
        // Animations that are canceled will have had their animator reassigned, and this check won't pass.
        if (getAnimator(v) == animation) {
            v.setVisibility(INVISIBLE);
        }
        setAnimator(v, null);
    }

    private void animate(@NonNull View v, @NonNull Animator animator, boolean immediately) {
        cancelAnimator(v);
        setAnimator(v, animator);
        if (immediately) {
            animator.setDuration(0);
        }
        animator.setTarget(v);
        animator.start();
    }

    @Nullable
    private Animator createAnimatorIn() {
        return mAnimatorIn != null ? mAnimatorIn.clone() : null;
    }

    @Nullable
    private Animator createAnimatorOut() {
        return mAnimatorOut != null ? mAnimatorOut.clone() : null;
    }

    private static void cancelAnimator(@NonNull View v) {
        Animator animator = getAnimator(v);
        if (animator != null) {
            // Must reassign animator before cancel.
            setAnimator(v, null);
            animator.cancel();
        }
    }

    private static void setAnimator(@NonNull View v, @Nullable Animator animator) {
        v.setTag(R.id.data_layout_animator, animator);
    }

    @Nullable
    private static Animator getAnimator(@NonNull View v) {
        return (Animator) v.getTag(R.id.data_layout_animator);
    }

    @Nullable
    private static Animator loadAnimator(@NonNull Context context,
                                         @NonNull TypedArray typedArray,
                                         int index,
                                         @AnimatorRes int defaultValue) {
        return AnimatorInflater.loadAnimator(context, typedArray.getResourceId(index, defaultValue));
    }

    //endregion

    @NonNull
    private static String name(@NonNull View v) {
        return v.getResources().getResourceEntryName(v.getId());
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
         * @param dataLayout The parent {@link DataLayout}.
         * @param v The child component view being checked for visibility.
         * @return {@code true} makes the view visible, {@code false} makes it invisible.
         */
        boolean shouldShow(@NonNull DataLayout dataLayout, @NonNull View v);
    }
}
