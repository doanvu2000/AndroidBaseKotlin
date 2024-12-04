package com.base.simplecropview.animation;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;

public class ValueAnimatorV14
        implements SimpleValueAnimator, Animator.AnimatorListener,
        ValueAnimator.AnimatorUpdateListener {
    private static final int DEFAULT_ANIMATION_DURATION = 150;
    private ValueAnimator animator;
    private SimpleValueAnimatorListener animatorListener = new SimpleValueAnimatorListener() {
        @Override
        public void onAnimationStarted() {

        }

        @Override
        public void onAnimationUpdated(float scale) {

        }

        @Override
        public void onAnimationFinished() {

        }
    };

    public ValueAnimatorV14(Interpolator interpolator) {
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.addListener(this);
        animator.addUpdateListener(this);
        animator.setInterpolator(interpolator);
    }

    @Override
    public void startAnimation(long duration) {
        if (duration >= 0) {
            animator.setDuration(duration);
        } else {
            animator.setDuration(DEFAULT_ANIMATION_DURATION);
        }
        animator.start();
    }

    @Override
    public void cancelAnimation() {
        animator.cancel();
    }

    @Override
    public boolean isAnimationStarted() {
        return animator.isStarted();
    }

    @Override
    public void addAnimatorListener(SimpleValueAnimatorListener animatorListener) {
        if (animatorListener != null) this.animatorListener = animatorListener;
    }

    @Override
    public void onAnimationStart(@NonNull Animator animation) {
        animatorListener.onAnimationStarted();
    }

    @Override
    public void onAnimationEnd(@NonNull Animator animation) {
        animatorListener.onAnimationFinished();
    }

    @Override
    public void onAnimationCancel(@NonNull Animator animation) {
        animatorListener.onAnimationFinished();
    }

    @Override
    public void onAnimationRepeat(@NonNull Animator animation) {

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        animatorListener.onAnimationUpdated(animation.getAnimatedFraction());
    }
}
