package com.base.cameraview.engine.mappers

import android.hardware.camera2.CameraCharacteristics
import android.util.Pair
import com.base.cameraview.controls.Control
import com.base.cameraview.controls.Facing
import com.base.cameraview.controls.Flash
import com.base.cameraview.controls.Hdr
import com.base.cameraview.controls.WhiteBalance

/**
 * A Mapper maps camera engine constants to CameraView constants.
 */
class Camera2Mapper private constructor() {
    fun mapFlash(flash: Flash): MutableList<Pair<Int?, Int?>?> {
        val result: MutableList<Pair<Int?, Int?>?> = ArrayList<Pair<Int?, Int?>?>()
        when (flash) {
            Flash.ON -> {
                result.add(
                    Pair<Int?, Int?>(
                        CameraCharacteristics.CONTROL_AE_MODE_ON_ALWAYS_FLASH,
                        CameraCharacteristics.FLASH_MODE_OFF
                    )
                )
            }

            Flash.AUTO -> {
                result.add(
                    Pair<Int?, Int?>(
                        CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH,
                        CameraCharacteristics.FLASH_MODE_OFF
                    )
                )
                result.add(
                    Pair<Int?, Int?>(
                        CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE,
                        CameraCharacteristics.FLASH_MODE_OFF
                    )
                )
            }

            Flash.OFF -> {
                result.add(
                    Pair<Int?, Int?>(
                        CameraCharacteristics.CONTROL_AE_MODE_ON,
                        CameraCharacteristics.FLASH_MODE_OFF
                    )
                )
                result.add(
                    Pair<Int?, Int?>(
                        CameraCharacteristics.CONTROL_AE_MODE_OFF,
                        CameraCharacteristics.FLASH_MODE_OFF
                    )
                )
            }

            Flash.TORCH -> {
                // When AE_MODE is ON or OFF, we can finally use the flash mode
                // low level control to either turn flash off or open the torch
                result.add(
                    Pair<Int?, Int?>(
                        CameraCharacteristics.CONTROL_AE_MODE_ON,
                        CameraCharacteristics.FLASH_MODE_TORCH
                    )
                )
                result.add(
                    Pair<Int?, Int?>(
                        CameraCharacteristics.CONTROL_AE_MODE_OFF,
                        CameraCharacteristics.FLASH_MODE_TORCH
                    )
                )
            }
        }
        return result
    }

    fun mapFacing(facing: Facing): Int {
        return FACING[facing]!!
    }

    fun mapWhiteBalance(whiteBalance: WhiteBalance): Int {
        return WB[whiteBalance]!!
    }

    fun mapHdr(hdr: Hdr): Int {
        return HDR[hdr]!!
    }

    fun unmapFlash(cameraConstant: Int): MutableSet<Flash?> {
        val result: MutableSet<Flash?> = HashSet()
        when (cameraConstant) {
            CameraCharacteristics.CONTROL_AE_MODE_OFF, CameraCharacteristics.CONTROL_AE_MODE_ON -> {
                result.add(Flash.OFF)
                result.add(Flash.TORCH)
            }

            CameraCharacteristics.CONTROL_AE_MODE_ON_ALWAYS_FLASH -> {
                result.add(Flash.ON)
            }

            CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH, CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE -> {
                result.add(Flash.AUTO)
            }

            CameraCharacteristics.CONTROL_AE_MODE_ON_EXTERNAL_FLASH -> {}
            else -> {}
        }
        return result
    }

    fun unmapFacing(cameraConstant: Int): Facing? {
        return reverseLookup<Facing?, Int>(FACING, cameraConstant)
    }

    fun unmapWhiteBalance(cameraConstant: Int): WhiteBalance? {
        return reverseLookup<WhiteBalance?, Int>(WB, cameraConstant)
    }

    fun unmapHdr(cameraConstant: Int): Hdr? {
        return reverseLookup<Hdr?, Int>(HDR, cameraConstant)
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
        private val FACING: MutableMap<Facing?, Int> = HashMap<Facing?, Int>()
        private val WB: MutableMap<WhiteBalance?, Int> = HashMap<WhiteBalance?, Int>()
        private val HDR: MutableMap<Hdr?, Int> = HashMap<Hdr?, Int>()
        private var sInstance: Camera2Mapper? = null

        init {
            FACING.put(Facing.BACK, CameraCharacteristics.LENS_FACING_BACK)
            FACING.put(Facing.FRONT, CameraCharacteristics.LENS_FACING_FRONT)
            WB.put(WhiteBalance.AUTO, CameraCharacteristics.CONTROL_AWB_MODE_AUTO)
            WB.put(WhiteBalance.CLOUDY, CameraCharacteristics.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT)
            WB.put(WhiteBalance.DAYLIGHT, CameraCharacteristics.CONTROL_AWB_MODE_DAYLIGHT)
            WB.put(WhiteBalance.FLUORESCENT, CameraCharacteristics.CONTROL_AWB_MODE_FLUORESCENT)
            WB.put(WhiteBalance.INCANDESCENT, CameraCharacteristics.CONTROL_AWB_MODE_INCANDESCENT)
            HDR.put(Hdr.OFF, CameraCharacteristics.CONTROL_SCENE_MODE_DISABLED)
            HDR.put(Hdr.ON, 18 /* CameraCharacteristics.CONTROL_SCENE_MODE_HDR */)
        }

        @JvmStatic
        fun get(): Camera2Mapper {
            if (sInstance == null) {
                sInstance = Camera2Mapper()
            }
            return sInstance!!
        }
    }
}
