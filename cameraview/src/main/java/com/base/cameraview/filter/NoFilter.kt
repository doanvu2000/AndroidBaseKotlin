package com.base.cameraview.filter

/**
 * A [Filter] that draws frames without any modification.
 */
class NoFilter : BaseFilter() {
    override val fragmentShader: String
        get() = createDefaultFragmentShader()
}
