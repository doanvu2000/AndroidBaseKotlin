package com.base.cameraview.controls

/**
 * The engine to be used.
 */
enum class Engine(val value: Int) : Control {
    /**
     * Camera1 based engine.
     */
    CAMERA1(0),

    /**
     * Camera2 based engine. For API versions older than 21,
     * the system falls back to [.CAMERA1].
     */
    CAMERA2(1);

    companion object {
        @JvmField
        val DEFAULT: Engine = Engine.CAMERA1

        @JvmStatic
        fun fromValue(value: Int): Engine {
            val list = Engine.entries.toTypedArray()
            for (action in list) {
                if (action.value == value) {
                    return action
                }
            }
            return DEFAULT
        }
    }
}
