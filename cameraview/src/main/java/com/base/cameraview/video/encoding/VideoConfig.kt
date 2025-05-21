package com.base.cameraview.video.encoding

/**
 * Base video configuration to be passed as input to the constructor
 * of a [VideoMediaEncoder].
 */
open class VideoConfig {
    @JvmField
    var width: Int = 0

    @JvmField
    var height: Int = 0

    @JvmField
    var bitRate: Int = 0

    @JvmField
    var frameRate: Int = 0

    @JvmField
    var rotation: Int = 0

    @JvmField
    var mimeType: String? = null

    @JvmField
    var encoder: String? = null

    protected fun <C : VideoConfig?> copy(output: C) {
        output?.width = this.width
        output?.height = this.height
        output?.bitRate = this.bitRate
        output?.frameRate = this.frameRate
        output?.rotation = this.rotation
        output?.mimeType = this.mimeType
        output?.encoder = this.encoder
    }
}
