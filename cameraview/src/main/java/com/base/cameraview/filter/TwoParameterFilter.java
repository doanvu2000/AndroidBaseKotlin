package com.base.cameraview.filter;

/**
 * A special {@link Filter} that accepts two floats parameters.
 * This is done by extending {@link OneParameterFilter}.
 * <p>
 * The parameters will always be between 0F and 1F, so subclasses should
 * map this range to their internal range if needed.
 * <p>
 * A standardized range is useful for different applications. For example:
 * - Filter parameters can be easily mapped to gestures since the range is fixed
 * - {@link BaseFilter} can use this setters and getters to make a filter copy
 */
public interface TwoParameterFilter extends OneParameterFilter {

    /**
     * Returns the second parameter.
     * The returned value should always be between 0 and 1.
     *
     * @return parameter
     */
    float getParameter2();

    /**
     * Sets the second parameter.
     * The value should always be between 0 and 1.
     *
     * @param value parameter
     */
    void setParameter2(float value);
}
