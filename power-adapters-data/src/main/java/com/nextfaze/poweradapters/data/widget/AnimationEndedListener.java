package com.nextfaze.poweradapters.data.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

abstract class AnimationEndedListener extends AnimatorListenerAdapter {

    private boolean mCanceled;

    @Override
    public void onAnimationCancel(Animator animation) {
        mCanceled = true;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (!mCanceled) {
            onEnd(animation);
        }
    }

    abstract void onEnd(Animator animation);
}
