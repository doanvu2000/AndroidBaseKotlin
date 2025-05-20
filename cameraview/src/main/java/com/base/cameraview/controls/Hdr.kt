package com.base.cameraview.controls

/**
 * Hdr values indicate whether to use high dynamic range techniques when capturing pictures.
 */
enum class Hdr(val value: Int) : Control {
    /**
     * No HDR.
     */
    OFF(0),

    /**
     * Using HDR.
     */
    ON(1);

    companion object {
        @JvmField
        val DEFAULT: Hdr = OFF

        @JvmStatic
        fun fromValue(value: Int): Hdr {
            val list = Hdr.entries.toTypedArray()
            for (action in list) {
                if (action.value == value) {
                    return action
                }
            }
            return DEFAULT
        }
    }
}
