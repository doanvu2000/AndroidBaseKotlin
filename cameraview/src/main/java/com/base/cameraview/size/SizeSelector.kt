package com.base.cameraview.size

/**
 * A size selector receives a list of [Size]s and returns another list with
 * sizes that are considered acceptable.
 */
interface SizeSelector {
    /**
     * Returns a list of acceptable sizes from the given input.
     * The final size will be the first element in the output list.
     *
     * @param source input list
     * @return output list
     */
    fun select(source: MutableList<Size?>): MutableList<Size?>
}
