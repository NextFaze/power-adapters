package com.nextfaze.poweradapters.data.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.AnimatorRes;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.R;
import com.nextfaze.poweradapters.data.internal.DataWatcher;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.SystemClock.elapsedRealtime;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

/**
 * A container view that, when hooked up to {@link Data} instance(s), will automatically show/hide child component
 * views based on the state of the data.
 * Child views are assigned components using the {@link R.styleable#DataLayout_Layout_layout_component} attribute.
 * Each {@link DataLayout} can optionally contain the following components as child views:
 * <h2>Content View</h2>
 * An {@link AdapterView} of some kind, or a {@code RecyclerView} (the type is not checked, it can be any view).
 * It can be assigned using {@link DataLayout#CONTENT}.
 * <h2>Empty View</h2>
 * An empty view, which will be shown while the {@link Data} is empty. It can be assigned using {@link
 * DataLayout#EMPTY}.
 * <h2>Loading View</h2>
 * A loading view, which will be shown while the {@link Data} is empty and loading. It can be assigned using {@link
 * DataLayout#LOADING}.
 * <h2>Error View</h2>
 * An error view, which will be shown if the {@link Data} emits an error. It can be assigned using {@link
 * DataLayout#ERROR}.
 * Components can also be updated programmatically by configuring the child view {@link DataLayout.LayoutParams}.
 * @see DataLayout.LayoutParams
 * @see DataLayout#invalidateComponentMapping()
 */
public class DataLayout extends RelativeLayout {

    private static final String TAG = DataLayout.class.getSimpleName();

    private static final String KEY_SUPER_STATE = "superState";
    private static final String KEY_ERROR_MESSAGE = "errorMessage";

    public static final int NONE = 0;
    public static final int CONTENT = 1;
    public static final int EMPTY = 2;
    public static final int LOADING = 3;
    public static final int ERROR = 4;

    @AnimatorRes
    private static final int DEFAULT_ANIMATION_IN = R.animator.data_layout_default_in;

    @AnimatorRes
    private static final int DEFAULT_ANIMATION_OUT = R.animator.data_layout_default_out;

    @NonNull
    private final Rect mRect = new Rect();

    @NonNull
    private final ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            updateVisible();
        }
    };

    @NonNull
    private final DataWatcher mDataWatcher = new DataWatcher() {
        @Override
        public void onDataChange() {
            updateViews();
        }

        @Override
        public void onDataLoadingChange() {
            if (isLoading()) {
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

    @NonNull
    private final WeakHashMap<View, Animator> mAnimators = new WeakHashMap<>();

    /** Child view ID for displaying contents of {@link #mDatas}. */
    @IdRes
    private final int mContentViewId;

    /** Child view ID shown while {@link #isEmpty()}. */
    @IdRes
    private final int mEmptyViewId;

    /** Child view ID shown while {@link #isLoading()}. */
    @IdRes
    private final int mLoadingViewId;

    /** Child view ID shown when {@link #mDatas} report an error. */
    @IdRes
    private final int mErrorViewId;

    /** Child view for displaying contents of the data. */
    @Nullable
    private View mContentView;

    /** Child view shown while {@link #isEmpty()}. */
    @Nullable
    private View mEmptyView;

    /** Child view shown while {@link #isLoading()}. */
    @Nullable
    private View mLoadingView;

    /** Child view shown when {@link #mDatas} report an error. */
    @Nullable
    private View mErrorView;

    /** The data instances used to determine child view visibility. */
    @NonNull
    private final List<Data<?>> mDatas = new ArrayList<>();

    /** The current error message, if any. */
    @Nullable
    CharSequence mErrorMessage;

    /** Formats error messages to be displayed in the error view. */
    @Nullable
    private ErrorFormatter mErrorFormatter = ErrorFormatter.DEFAULT;

    @NonNull
    private VisibilityPolicy mVisibilityPolicy = VisibilityPolicy.DEFAULT;

    @Nullable
    private OnVisibleChangeListener mOnVisibleChangeListener;

    @Nullable
    private OnVisibleComponentChangeListener mOnVisibleComponentChangeListener;

    @Nullable
    private OnErrorListener mOnErrorListener;

    /** Animator used to show views. */
    @Nullable
    private Animator mAnimatorIn;

    /** Animator used to hide views. */
    @Nullable
    private Animator mAnimatorOut;

    @Nullable
    private View mVisibleView;

    int mComponentVisibilityWhenHidden = INVISIBLE;

    /** Indicates this view is attached to the window. */
    private boolean mAttachedToWindow;

    /** Indicates this view is visible to the user. */
    private boolean mVisible;

    /** Track when this view became visible. */
    private long mVisibleStartTime;

    /** Used to work around NPE caused by {@link #onVisibilityChanged(View, int)} self call in super class. */
    private boolean mInflated;

    /** Indicates animations will run as inner views show and hide. */
    private boolean mAnimationEnabled = true;

    @NonNull
    private VisibilityPredicate mVisibilityPredicate = VisibilityPredicate.DEFAULT;

    public DataLayout(Context context) {
        this(context, null);
    }

    public DataLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DataLayout, defStyle, 0);
        try {
            mContentViewId = a.getResourceId(R.styleable.DataLayout_contentView, -1);
            mEmptyViewId = a.getResourceId(R.styleable.DataLayout_emptyView, -1);
            mLoadingViewId = a.getResourceId(R.styleable.DataLayout_loadingView, -1);
            mErrorViewId = a.getResourceId(R.styleable.DataLayout_errorView, -1);
            // Use old deprecated attributes as fallbacks.
            mAnimatorIn = loadAnimator(context, a, R.styleable.DataLayout_data_layout_animatorIn,
                    R.styleable.DataLayout_animatorIn, DEFAULT_ANIMATION_IN);
            mAnimatorOut = loadAnimator(context, a, R.styleable.DataLayout_data_layout_animatorOut,
                    R.styleable.DataLayout_animatorOut, DEFAULT_ANIMATION_OUT);
            mAnimationEnabled = a.getBoolean(R.styleable.DataLayout_data_layout_animationsEnabled,
                    a.getBoolean(R.styleable.DataLayout_animationsEnabled, mAnimationEnabled));
            mComponentVisibilityWhenHidden = a.getInt(R.styleable.DataLayout_data_layout_componentVisibilityWhenHidden,
                    mComponentVisibilityWhenHidden);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!mInflated) {
            mInflated = true;

            // Assign components based on deprecated view ID attributes first.
            mContentView = findViewById(mContentViewId);
            mEmptyView = findViewById(mEmptyViewId);
            mLoadingView = findViewById(mLoadingViewId);
            mErrorView = findViewById(mErrorViewId);

            // Now use the layout params and possibly override.
            detectAndAssignComponents();

            // All components are initially invisible.
            if (mContentView != null) {
                hideComponent(mContentView);
            }
            if (mEmptyView != null) {
                hideComponent(mEmptyView);
            }
            if (mLoadingView != null) {
                hideComponent(mLoadingView);
            }
            if (mErrorView != null) {
                hideComponent(mErrorView);
            }

            updateViews();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateVisible();
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(@NonNull ViewGroup.LayoutParams p) {
        return new LayoutParams(super.generateLayoutParams(p));
    }

    @Override
    public RelativeLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
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
        getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener);
        mAttachedToWindow = true;
        updateVisible();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
        mAttachedToWindow = false;
        updateVisible();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            updateVisible();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (mInflated && visibility == VISIBLE) {
            updateVisible();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mInflated) {
            updateVisible();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateVisible();
    }

    /**
     * Connects this view to a {@link Data} instance, so it can observe its loading/error/empty state and adjust child
     * view visibility accordingly.
     * @param data The data instance to observe, which may be {@code null} to cease observing anything.
     */
    public final void setData(@Nullable Data<?> data) {
        setDatas(data != null ? singleton(data) : Collections.<Data<?>>emptyList());
    }

    /** @see #setDatas(Iterable) */
    public final void setDatas(@NonNull Data<?>... datas) {
        setDatas(asList(checkNotNull(datas, "datas")));
    }

    /**
     * Connects this view to the specified {@link Data} instances, so it can observe their loading/error/empty state
     * and adjust child view visibility accordingly.
     * @param datas The data instances to observe, which may be empty to cease observing anything.
     */
    public final void setDatas(@NonNull Iterable<? extends Data<?>> datas) {
        checkNotNull(datas, "datas");
        mDataWatcher.setDatas(datas);
        mDatas.clear();
        for (Data<?> data : datas) {
            mDatas.add(data);
        }
        // Clear error message, because the caller expects the view state to be reset when all datas change
        if (mDatas.isEmpty()) {
            mErrorMessage = null;
        }
        updateViews();
    }

    /** Returns a copy of the {@link Data} instances being observed. */
    @NonNull
    public final List<Data<?>> getDatas() {
        return new ArrayList<>(mDatas);
    }

    /** Adds a {@link Data} to be observed by this layout. */
    public final void addData(@NonNull Data<?> data) {
        checkNotNull(data, "data");
        // Clear error message now, in case the caller recently replaced them.
        // If they did, they'd expect the error message to be gone, because the
        // view visibility state is reset.
        if (mDatas.isEmpty()) {
            mErrorMessage = null;
        }
        mDatas.add(data);
        mDataWatcher.setDatas(mDatas);
        updateViews();
    }

    /** Removes a {@link Data} so that it will no longer be observed by this layout. */
    public final void removeData(@NonNull Data<?> data) {
        checkNotNull(data, "data");
        mDatas.remove(data);
        mDataWatcher.setDatas(mDatas);
        updateViews();
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
        checkNotNull(visibilityPolicy, "visibilityPolicy");
        if (visibilityPolicy != mVisibilityPolicy) {
            mVisibilityPolicy = visibilityPolicy;
            updateViews();
        }
    }

    @NonNull
    public final VisibilityPredicate getVisibilityPredicate() {
        return mVisibilityPredicate;
    }

    public final void setVisibilityPredicate(@NonNull VisibilityPredicate visibilityPredicate) {
        checkNotNull(visibilityPredicate, "visibilityPredicate");
        if (visibilityPredicate != mVisibilityPredicate) {
            mVisibilityPredicate = visibilityPredicate;
            updateVisible();
        }
    }

    @Nullable
    public final OnVisibleChangeListener getOnVisibleChangeListener() {
        return mOnVisibleChangeListener;
    }

    public final void setOnVisibleChangeListener(@Nullable OnVisibleChangeListener onVisibleChangeListener) {
        mOnVisibleChangeListener = onVisibleChangeListener;
    }

    @Nullable
    public OnVisibleComponentChangeListener getOnVisibleComponentChangeListener() {
        return mOnVisibleComponentChangeListener;
    }

    public void setOnVisibleComponentChangeListener(@Nullable OnVisibleComponentChangeListener onVisibleComponentChangeListener) {
        mOnVisibleComponentChangeListener = onVisibleComponentChangeListener;
    }

    @Nullable
    public final OnErrorListener getOnErrorListener() {
        return mOnErrorListener;
    }

    public final void setOnErrorListener(@Nullable OnErrorListener onErrorListener) {
        if (onErrorListener != mOnErrorListener) {
            mOnErrorListener = onErrorListener;
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
        setComponent(CONTENT, contentView);
    }

    @Nullable
    public final View getEmptyView() {
        return mEmptyView;
    }

    public final void setEmptyView(@Nullable View emptyView) {
        setComponent(EMPTY, emptyView);
    }

    @Nullable
    public final View getLoadingView() {
        return mLoadingView;
    }

    public final void setLoadingView(@Nullable View loadingView) {
        setComponent(LOADING, loadingView);
    }

    @Nullable
    public final View getErrorView() {
        return mErrorView;
    }

    public final void setErrorView(@Nullable View errorView) {
        setComponent(ERROR, errorView);
    }

    public final void setComponent(@Component int component, @Nullable View v) {
        if (assignComponent(component, v)) {
            updateViews();
        }
    }

    @Component
    public final int getComponent(@NonNull View v) {
        checkNotNull(v, "v");
        if (v == mContentView) {
            return CONTENT;
        } else if (v == mEmptyView) {
            return EMPTY;
        } else if (v == mLoadingView) {
            return LOADING;
        } else if (v == mErrorView) {
            return ERROR;
        }
        return NONE;
    }

    @ComponentVisibilityWhenHidden
    public final int getComponentVisibilityWhenHidden() {
        return mComponentVisibilityWhenHidden;
    }

    /** Sets the visibility mode to apply when hiding a component view, which is {@link View#INVISIBLE} by default. */
    public final void setComponentVisibilityWhenHidden(@ComponentVisibilityWhenHidden int componentVisibilityWhenHidden) {
        assertValidComponentVisibilityWhenHidden(componentVisibilityWhenHidden);
        mComponentVisibilityWhenHidden = componentVisibilityWhenHidden;
    }

    private void assertValidComponentVisibilityWhenHidden(int componentVisibilityWhenHidden) {
        if (componentVisibilityWhenHidden != INVISIBLE && componentVisibilityWhenHidden != GONE) {
            throw new IllegalArgumentException("componentVisibilityWhenHidden must be INVISIBLE or GONE");
        }
    }

    private boolean detectAndAssignComponents() {
        boolean changed = false;
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            ViewGroup.LayoutParams params = v.getLayoutParams();
            if (params instanceof LayoutParams) {
                LayoutParams layoutParams = (LayoutParams) params;
                changed = assignComponent(layoutParams.getComponent(), v) || changed;
            }
        }
        return changed;
    }

    private boolean assignComponent(@Component int component, @Nullable View v) {
        switch (component) {
            case CONTENT:
                if (v != mContentView) {
                    mContentView = v;
                    return true;
                }
                break;

            case EMPTY:
                if (v != mEmptyView) {
                    mEmptyView = v;
                    return true;
                }
                break;

            case LOADING:
                if (v != mLoadingView) {
                    mLoadingView = v;
                    return true;
                }
                break;

            case ERROR:
                if (v != mErrorView) {
                    mErrorView = v;
                    return true;
                }
                break;
        }
        return false;
    }

    /** Returns if any of the observed {@link Data} instances are loading. */
    public final boolean isLoading() {
        if (mDatas.isEmpty()) {
            return false;
        }
        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i).isLoading()) {
                return true;
            }
        }
        return false;
    }

    /** Returns if all of the observed {@link Data} instances are empty, or if none are being observed. */
    public final boolean isEmpty() {
        if (mDatas.isEmpty()) {
            return true;
        }
        for (int i = 0; i < mDatas.size(); i++) {
            if (!mDatas.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Re-assigns the component mapping by inspecting the {@link DataLayout.LayoutParams} of child views. */
    public final void invalidateComponentMapping() {
        if (detectAndAssignComponents()) {
            updateViews();
        }
    }

    /** Indicates if this view is visible. The {@link Data} instances are observed while visible. */
    public final boolean isVisible() {
        return mVisible;
    }

    /**
     * Called when the visible state of this view changes. The {@link Data} instances are observed while this
     * view is visible.
     * @see #isVisible()
     * @see #setOnVisibleChangeListener(OnVisibleChangeListener)
     */
    @SuppressWarnings("UnusedParameters")
    protected void onVisibleChanged(boolean visible) {
    }

    private void dispatchVisibleChanged(boolean visible) {
        if (mOnVisibleChangeListener != null) {
            mOnVisibleChangeListener.onVisibleChanged(visible);
        }
    }

    private void dispatchVisibleComponentChanged(@Nullable View oldComponent, @Nullable View newComponent) {
        if (mOnVisibleComponentChangeListener != null) {
            mOnVisibleComponentChangeListener.onVisibleComponentChange(oldComponent, newComponent);
        }
    }

    /** Called to apply an error message when an error occurs. Assigns the text to the error view by default. */
    protected void applyErrorMessage(@Nullable CharSequence errorMessage) {
        if (mErrorView instanceof TextView) {
            ((TextView) mErrorView).setText(errorMessage);
        }
    }

    private void dispatchError(@Nullable CharSequence errorMessage) {
        if (mOnErrorListener != null) {
            mOnErrorListener.onError(errorMessage);
        }
    }

    void updateVisible() {
        boolean visible = mVisibilityPredicate.isVisible(this);
        mDataWatcher.setEnabled(visible);
        if (visible != mVisible) {
            mVisible = visible;
            onVisibleChanged(visible);
            dispatchVisibleChanged(visible);
            mVisibleStartTime = visible ? elapsedRealtime() : 0;
            updateViews();
        }
    }

    public final boolean isAttached() {
        return mAttachedToWindow;
    }

    public final float getVisibleAreaFactor() {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return 0f;
        }
        if (getGlobalVisibleRect(mRect)) {
            float visibleArea = mRect.width() * mRect.height();
            float viewArea = width * height;
            return max(min(1f, visibleArea / viewArea), 0f);
        }
        return 0f;
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
        if (mVisibleStartTime <= 0) {
            return false;
        }
        long threshold = (long) (1000 / display.getRefreshRate());
        long millisVisible = elapsedRealtime() - mVisibleStartTime;
        return millisVisible > threshold;
    }

    /** Get a {@link Display} object suitable for checking the refresh rate. */
    @Nullable
    private Display currentDisplay() {
        if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
            return getDisplay();
        } else {
            Context context = getContext();
            if (!(context instanceof Activity)) {
                return null;
            }
            return ((Activity) context).getWindowManager().getDefaultDisplay();
        }
    }

    void updateViews() {
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
            dispatchVisibleComponentChanged(oldView, newView);
        }
    }

    @Nullable
    private View viewToBeShown() {
        if (mLoadingView != null && mVisibilityPolicy.shouldShow(this, mLoadingView)) {
            return mLoadingView;
        }
        if (mErrorView != null && mVisibilityPolicy.shouldShow(this, mErrorView)) {
            return mErrorView;
        }
        if (mEmptyView != null && mVisibilityPolicy.shouldShow(this, mEmptyView)) {
            return mEmptyView;
        }
        if (mContentView != null && mVisibilityPolicy.shouldShow(this, mContentView)) {
            return mContentView;
        }
        return null;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    boolean shouldShow(@NonNull View v) {
        if (v == mLoadingView) {
            return isLoading() && isEmpty();
        }
        if (v == mEmptyView) {
            return isEmpty();
        }
        if (v == mErrorView) {
            return mErrorMessage != null;
        }
        if (v == mContentView) {
            return !mDatas.isEmpty();
        }
        return false;
    }

    void updateErrorView() {
        applyErrorMessage(mErrorMessage);
        dispatchError(mErrorMessage);
    }

    @Nullable
    CharSequence formatErrorMessage(@NonNull Throwable e) {
        if (mErrorFormatter == null) {
            return null;
        }
        return mErrorFormatter.format(getContext(), e);
    }

    private void animateIn(@NonNull View v, boolean immediately) {
        v.setVisibility(VISIBLE);
        Animator animator = mAnimatorIn != null ? mAnimatorIn.clone() : null;
        if (animator != null) {
            animate(v, animator, immediately);
        }
    }

    private void animateOut(@NonNull final View v, boolean immediately) {
        Animator animator = mAnimatorOut != null ? mAnimatorOut.clone() : null;
        if (animator != null) {
            animator.addListener(new AnimationEndedListener() {
                @Override
                void onEnd(Animator animation) {
                    v.setVisibility(mComponentVisibilityWhenHidden);
                }
            });
            animate(v, animator, immediately);
        }
    }

    private void animate(@NonNull final View v, @NonNull Animator animator, boolean immediately) {
        Animator existing = mAnimators.put(v, animator);
        if (existing != null) {
            existing.cancel();
        }
        if (immediately) {
            animator.setDuration(0);
        }
        animator.setTarget(v);
        animator.start();
    }

    private void hideComponent(@Nullable View v) {
        if (v != null) {
            v.setVisibility(mComponentVisibilityWhenHidden);
            // Run the "out" animation immediately to ensure the component view is in the proper state.
            // Eg. the force the view to alpha 0.
            if (mAnimatorOut != null) {
                Animator animator = mAnimatorOut.clone();
                animator.setDuration(0);
                animator.setTarget(v);
                animator.start();
            }
        }
    }

    @Nullable
    private static Animator loadAnimator(@NonNull Context context,
                                         @NonNull TypedArray typedArray,
                                         int index,
                                         int fallbackIndex,
                                         @AnimatorRes int defaultValue) {
        return AnimatorInflater.loadAnimator(context,
                typedArray.getResourceId(index, typedArray.getResourceId(fallbackIndex, defaultValue)));
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
         * <li>Loading</li>
         * <li>Error</li>
         * <li>Empty</li>
         * <li>Content</li>
         * </ol>
         * The view of the first callback to return {@code true} will be made visible.
         * @param dataLayout The parent {@link DataLayout}.
         * @param v The child component view being checked for visibility.
         * @return {@code true} makes the view visible, {@code false} hides it, using the visibility specified in {@link
         * #setComponentVisibilityWhenHidden(int)}.
         */
        boolean shouldShow(@NonNull DataLayout dataLayout, @NonNull View v);
    }

    /** Returns if this view is currently considered "shown" based on various attributes. */
    public interface VisibilityPredicate {
        VisibilityPredicate DEFAULT = new VisibilityPredicate() {
            @Override
            public boolean isVisible(@NonNull DataLayout dataLayout) {
                return dataLayout.isAttached() &&
                        dataLayout.getWindowVisibility() == VISIBLE &&
                        dataLayout.getVisibility() == VISIBLE &&
                        dataLayout.isShown() &&
                        dataLayout.isEnabled() &&
                        dataLayout.getVisibleAreaFactor() > 0f;
            }
        };

        boolean isVisible(@NonNull DataLayout dataLayout);
    }

    /** Callback interface for when the visible state of this view changes. */
    public interface OnVisibleChangeListener {
        void onVisibleChanged(boolean visible);
    }

    /** Callback interface for when the visible component view changes. */
    public interface OnVisibleComponentChangeListener {
        void onVisibleComponentChange(@Nullable View oldComponent, @Nullable View newComponent);
    }

    /** Callback interface for when an error message is to be applied to the view hierarchy. */
    public interface OnErrorListener {
        void onError(@Nullable CharSequence errorMessage);
    }

    /** Denotes an integer value that should be one of the {@link DataLayout} child components. */
    @IntDef({ NONE, CONTENT, EMPTY, LOADING, ERROR })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Component {
    }

    /** Denotes an integer value corresponding to one of the {@link View} hidden visibility constants. */
    @IntDef({ INVISIBLE, GONE })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ComponentVisibilityWhenHidden {
    }

    /**
     * Layout params for {@link DataLayout} children, which is used to specified the component of the child.
     * @see DataLayout#CONTENT
     * @see DataLayout#EMPTY
     * @see DataLayout#LOADING
     * @see DataLayout#ERROR
     * @see DataLayout.Component
     */
    @SuppressWarnings("unused")
    public static class LayoutParams extends RelativeLayout.LayoutParams {

        @Component
        private int mComponent = NONE;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.DataLayout_Layout);
            try {
                //noinspection ResourceType
                mComponent = a.getInt(R.styleable.DataLayout_Layout_layout_component, mComponent);
            } finally {
                a.recycle();
            }
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public LayoutParams(RelativeLayout.LayoutParams source) {
            super(source);
        }

        /** Copy constructor. */
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public LayoutParams(LayoutParams source) {
            super(source);
            mComponent = source.mComponent;
        }

        /** @see #setComponent(int) */
        @Component
        public int getComponent() {
            return mComponent;
        }

        /**
         * Set the component represented by the child view attributed with these layout params. A change to the
         * component will only take effect after a call to {@link DataLayout#invalidateComponentMapping()}.
         * @param component The component enum int.
         */
        public void setComponent(@Component int component) {
            mComponent = component;
        }
    }
}
