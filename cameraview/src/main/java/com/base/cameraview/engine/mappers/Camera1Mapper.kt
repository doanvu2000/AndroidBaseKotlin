package com.base.cameraview.engine.mappers

import android.hardware.Camera
import com.base.cameraview.controls.Control
import com.base.cameraview.controls.Facing
import com.base.cameraview.controls.Flash
import com.base.cameraview.controls.Hdr
import com.base.cameraview.controls.WhiteBalance

/**
 * A Mapper maps camera engine constants to CameraView constants.
 */
class Camera1Mapper private constructor() {
    fun mapFlash(flash: Flash): String {
        return FLASH[flash]!!
    }

    fun mapFacing(facing: Facing): Int {
        return FACING[facing]!!
    }

    fun mapWhiteBalance(whiteBalance: WhiteBalance): String {
        return WB[whiteBalance]!!
    }

    fun mapHdr(hdr: Hdr): String {
        return HDR[hdr]!!
    }

    fun unmapFlash(cameraConstant: String): Flash? {
        return reverseLookup<Flash?, String>(FLASH, cameraConstant)
    }

    fun unmapFacing(cameraConstant: Int): Facing? {
        return reverseLookup<Facing?, Int>(FACING, cameraConstant)
    }

    fun unmapWhiteBalance(cameraConstant: String): WhiteBalance? {
        return reverseLookup<WhiteBalance?, String>(WB, cameraConstant)
    }

    fun unmapHdr(cameraConstant: String): Hdr? {
        return reverseLookup<Hdr?, String>(HDR, cameraConstant)
    }

    private fun <C : Control?, T> reverseLookup(map: MutableMap<C?, T>, `object`: T): C? {
        for (value in map.keys) {
            if (`object` == map[value]) {
                return value
            }
        }
        return null
    }

    companion object {
        private val FLASH: MutableMap<Flash?, String> = HashMap<Flash?, String>()
        private val WB: MutableMap<WhiteBalance?, String> = HashMap<WhiteBalance?, String>()
        private val FACING: MutableMap<Facing?, Int> = HashMap<Facing?, Int>()
        private val HDR: MutableMap<Hdr?, String> = HashMap<Hdr?, String>()
        private var sInstance: Camera1Mapper? = null


        init {
            @Suppress("DEPRECATION") FLASH.put(Flash.OFF, Camera.Parameters.FLASH_MODE_OFF)
            @Suppress("DEPRECATION") FLASH.put(Flash.ON, Camera.Parameters.FLASH_MODE_ON)
            @Suppress("DEPRECATION") FLASH.put(Flash.AUTO, Camera.Parameters.FLASH_MODE_AUTO)
            @Suppress("DEPRECATION") FLASH.put(Flash.TORCH, Camera.Parameters.FLASH_MODE_TORCH)
            @Suppress("DEPRECATION") FACING.put(Facing.BACK, Camera.CameraInfo.CAMERA_FACING_BACK)
            @Suppress("DEPRECATION") FACING.put(Facing.FRONT, Camera.CameraInfo.CAMERA_FACING_FRONT)
            @Suppress("DEPRECATION") WB.put(WhiteBalance.AUTO, Camera.Parameters.WHITE_BALANCE_AUTO)
            @Suppress("DEPRECATION") WB.put(
                WhiteBalance.INCANDESCENT,
                Camera.Parameters.WHITE_BALANCE_INCANDESCENT
            )
            @Suppress("DEPRECATION") WB.put(
                WhiteBalance.FLUORESCENT,
                Camera.Parameters.WHITE_BALANCE_FLUORESCENT
            )
            @Suppress("DEPRECATION") WB.put(
                WhiteBalance.DAYLIGHT,
                Camera.Parameters.WHITE_BALANCE_DAYLIGHT
            )
            @Suppress("DEPRECATION") WB.put(
                WhiteBalance.CLOUDY,
                Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT
            )
            @Suppress("DEPRECATION") HDR.put(Hdr.OFF, Camera.Parameters.SCENE_MODE_AUTO)
            @Suppress("DEPRECATION") HDR.put(Hdr.ON, Camera.Parameters.SCENE_MODE_HDR)
        }

        @JvmStatic
        fun get(): Camera1Mapper {
            if (sInstance == null) {
                sInstance = Camera1Mapper()
            }
            return sInstance!!
        }
    }
}
