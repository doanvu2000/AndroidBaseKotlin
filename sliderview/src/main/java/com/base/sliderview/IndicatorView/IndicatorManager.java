package com.base.sliderview.IndicatorView;

import androidx.annotation.Nullable;

import com.base.sliderview.IndicatorView.animation.AnimationManager;
import com.base.sliderview.IndicatorView.animation.controller.ValueController;
import com.base.sliderview.IndicatorView.animation.data.Value;
import com.base.sliderview.IndicatorView.draw.DrawManager;
import com.base.sliderview.IndicatorView.draw.data.Indicator;

public class IndicatorManager implements ValueController.UpdateListener {

    private DrawManager drawManager;
    private AnimationManager animationManager;
    private Listener listener;

    IndicatorManager(@Nullable Listener listener) {
        this.listener = listener;
        this.drawManager = new DrawManager();
        this.animationManager = new AnimationManager(drawManager.indicator(), this);
    }

    public AnimationManager animate() {
        return animationManager;
    }

    public Indicator indicator() {
        return drawManager.indicator();
    }

    public DrawManager drawer() {
        return drawManager;
    }

    @Override
    public void onValueUpdated(@Nullable Value value) {
        drawManager.updateValue(value);
        if (listener != null) {
            listener.onIndicatorUpdated();
        }
    }

    interface Listener {
        void onIndicatorUpdated();
    }
}
