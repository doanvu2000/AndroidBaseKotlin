package com.base.cameraview.filter


/**
 * A special [Filter] that accepts a float parameter.
 *
 *
 * The parameters will always be between 0F and 1F, so subclasses should
 * map this range to their internal range if needed.
 *
 *
 * A standardized range is useful for different applications. For example:
 * - Filter parameters can be easily mapped to gestures since the range is fixed
 * - [BaseFilter] can use this setters and getters to make a filter copy
 */
interface OneParameterFilter : Filter {
    /**
     * Returns the parameter.
     * The returned value should always be between 0 and 1.
     *
     * @return parameter
     */
    /**
     * Sets the parameter.
     * The value should always be between 0 and 1.
     */
    var parameter1: Float
}
