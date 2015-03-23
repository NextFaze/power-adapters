package com.nextfaze.databind.widget;

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
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nextfaze.databind.Data;
import com.nextfaze.databind.ErrorFormatter;
import com.nextfaze.databind.R;
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

    private static final String TAG = DataLayout.class.getSimpleName();
    private static final String KEY_SUPER_STATE = "superState";
    private static final String KEY_ERROR_MESSAGE = "errorMessage";

    @NonNull
    private final DataWatcher mDataWatcher = new DataWatcher() {
        @Override
        public void onChange() {
            updateViews();
        }

        @Override
        public void onLoadingChange() {
            Data<?> data = getData();
            if (data != null && data.isLoading()) {
                // Loading has started again, so clear error status.
                mErrorMessage = null;
            }
            updateErrorView();
            updateViews();
        }

        @Override
        public void onError(@NonNull Throwable e) {
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

    /** Animator used to show views. */
    @Nullable
    private Animator mAnimatorIn;

    /** Animator used to hide views. */
    @Nullable
    private Animator mAnimatorOut;

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
        mAnimatorIn = loadAnimator(context, a, R.styleable.DataLayout_animatorIn, R.animator.data_layout_default_in);
        mAnimatorOut = loadAnimator(context, a, R.styleable.DataLayout_animatorOut, R.animator.data_layout_default_out);
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

    private void updateShown() {
        boolean shown = isThisViewShown();
        mDataWatcher.setShown(shown);
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
        boolean animated = shouldAnimate();
        if (mData == null) {
            // No data, show empty.
            if (mEmptyView != null) {
                hide(mContentView, animated);
            }
            if (mErrorMessage == null) {
                show(mEmptyView, animated);
                hide(mErrorView, animated);
            } else {
                hide(mEmptyView, animated);
                show(mErrorView, animated);
            }
            hide(mLoadingView, animated);
        } else {
            if (mData.isEmpty()) {
                if (mData.isLoading()) {
                    // Empty, but loading, so show loading.
                    if (mLoadingView != null) {
                        hide(mContentView, animated);
                    }
                    hide(mEmptyView, animated);
                    show(mLoadingView, animated);
                    hide(mErrorView, animated);
                } else {
                    if (mErrorMessage == null) {
                        // Empty, not loading, no error, so show empty.
                        if (mEmptyView != null) {
                            hide(mContentView, animated);
                        }
                        show(mEmptyView, animated);
                        hide(mErrorView, animated);
                    } else {
                        // Empty, not loading, but has an error, so show error.
                        if (mErrorView != null) {
                            hide(mContentView, animated);
                        }
                        hide(mEmptyView, animated);
                        show(mErrorView, animated);
                    }
                    hide(mLoadingView, animated);
                }
            } else {
                // Not empty, show adapter view.
                show(mContentView, animated);
                hide(mEmptyView, animated);
                hide(mLoadingView, animated);
                hide(mErrorView, animated);
            }
        }
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

    private void show(@Nullable View v, boolean animated) {
        if (v == null) {
            return;
        }
        animateIn(v, !animated);
    }

    private void hide(@Nullable View v, boolean animated) {
        if (v == null) {
            return;
        }
        animateOut(v, !animated);
    }

    private void animateIn(@NonNull View v, boolean immediately) {
        if (isAnimatingIn(v)) {
            return;
        }
        Animator animator = createAnimatorIn();
        if (animator != null) {
            setAnimatingIn(v, true);
            animator.addListener(animateInListener(v));
            animate(v, animator, immediately);
        }
    }

    private void animateOut(@NonNull View v, boolean immediately) {
        if (isAnimatingOut(v)) {
            return;
        }
        Animator animator = createAnimatorOut();
        if (animator != null) {
            setAnimatingOut(v, true);
            animator.addListener(animateOutListener(v));
            animate(v, animator, immediately);
        }
    }

    private void animate(@NonNull View v, @NonNull Animator animator, boolean immediately) {
        cancelAnimator(v);
        setAnimator(v, animator);
        animator.addListener(animateOutListener(v));
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

    private static boolean isAnimating(@NonNull View v) {
        Animator animator = getAnimator(v);
        return animator != null && animator.isRunning();
    }

    private static void cancelAnimator(@NonNull View v) {
        Animator animator = getAnimator(v);
        if (animator != null) {
            animator.cancel();
            setAnimator(v, null);
        }
    }

    private static void setAnimator(@NonNull View v, @Nullable Animator animator) {
        v.setTag(R.id.data_layout_animator, animator);
    }

    @Nullable
    private static Animator getAnimator(@NonNull View v) {
        return (Animator) v.getTag(R.id.data_layout_animator);
    }

    private static boolean isAnimatingIn(@NonNull View v) {
        return getFlag(v, R.id.data_layout_animating_in);
    }

    private static void setAnimatingIn(@NonNull View v, boolean animatingIn) {
        v.setTag(R.id.data_layout_animating_in, animatingIn);
    }

    private static boolean isAnimatingOut(@NonNull View v) {
        return getFlag(v, R.id.data_layout_animating_out);
    }

    private static void setAnimatingOut(@NonNull View v, boolean animatingOut) {
        v.setTag(R.id.data_layout_animating_out, animatingOut);
    }

    private static boolean getFlag(@NonNull View v, @IdRes int tagId) {
        Boolean b = (Boolean) v.getTag(tagId);
        return b != null && b;
    }

    @NonNull
    private static Animator.AnimatorListener animateInListener(@NonNull final View v) {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setAnimator(v, null);
                setAnimatingIn(v, false);
            }
        };
    }

    @NonNull
    private static Animator.AnimatorListener animateOutListener(@NonNull final View v) {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setAnimator(v, null);
                setAnimatingOut(v, false);
            }
        };
    }

    @Nullable
    private static Animator loadAnimator(@NonNull Context context,
                                         @NonNull TypedArray typedArray,
                                         int index,
                                         @AnimatorRes int defaultValue) {
        return AnimatorInflater.loadAnimator(context, typedArray.getResourceId(index, defaultValue));
    }

    @NonNull
    private static String name(@NonNull View v) {
        return v.getResources().getResourceEntryName(v.getId());
    }
}
