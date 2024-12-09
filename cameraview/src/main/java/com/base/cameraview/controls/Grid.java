package com.base.cameraview.controls;


import androidx.annotation.NonNull;

/**
 * Grid values can be used to draw grid lines over the camera preview.
 */
public enum Grid implements Control {


    /**
     * No grid is drawn.
     */
    OFF(0),

    /**
     * Draws a regular, 3x3 grid.
     */
    DRAW_3X3(1),

    /**
     * Draws a regular, 4x4 grid.
     */
    DRAW_4X4(2),

    /**
     * Draws a grid respecting the 'phi' constant proportions,
     * often referred as to the golden ratio.
     */
    DRAW_PHI(3);

    static final Grid DEFAULT = OFF;

    private int value;

    Grid(int value) {
        this.value = value;
    }

    @NonNull
    static Grid fromValue(int value) {
        Grid[] list = Grid.values();
        for (Grid action : list) {
            if (action.value() == value) {
                return action;
            }
        }
        return DEFAULT;
    }

    int value() {
        return value;
    }
}