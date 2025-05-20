package com.base.cameraview.filter

/**
 * The simplest possible filter that accepts a fragment shader in its constructor.
 * This can be used when your fragment shader is static and has no 'runtime' parameters
 * that influence its behavior.
 *
 *
 * The given fragment shader should respect the default variable names, as listed
 * in the [BaseFilter] class.
 *
 *
 * NOTE: SimpleFilter is not meant to be subclassed!
 * Subclassing it would require you to override [.onCopy], which would make
 * this class useless. Instead, you can extend [BaseFilter] directly.
 */
class SimpleFilter
/**
 * Creates a new filter with the given fragment shader.
 *
 * @param fragmentShader a fragment shader
 */(override val fragmentShader: String) : BaseFilter() {
    override fun onCopy(): BaseFilter {
        return SimpleFilter(fragmentShader)
    }
}
