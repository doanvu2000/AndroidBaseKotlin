package com.base.cameraview.controls


/**
 * Grid values can be used to draw grid lines over the camera preview.
 */
enum class Grid(val value: Int) : Control {
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

    companion object {
        @JvmField
        val DEFAULT: Grid = OFF

        @JvmStatic
        fun fromValue(value: Int): Grid {
            val list = Grid.entries.toTypedArray()
            for (action in list) {
                if (action.value == value) {
                    return action
                }
            }
            return DEFAULT
        }
    }
}