package com.base.simplecropview.animation;

public interface SimpleValueAnimatorListener {
    void onAnimationStarted();

    void onAnimationUpdated(float scale);

    void onAnimationFinished();
}
