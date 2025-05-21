package com.base.cameraview.internal

import android.annotation.SuppressLint
import android.media.CamcorderProfile
import com.base.cameraview.CameraLogger
import com.base.cameraview.size.Size
import kotlin.math.abs

/**
 * Wraps the [CamcorderProfile] static utilities.
 */
object CamcorderProfiles {
    private val TAG: String = CamcorderProfiles::class.java.simpleName
    private val LOG: CameraLogger = CameraLogger.create(TAG)

    @SuppressLint("UseSparseArrays")
    private val sizeToProfileMap: MutableMap<Size?, Int?> = HashMap<Size?, Int?>()

    init {
        sizeToProfileMap.put(Size(176, 144), CamcorderProfile.QUALITY_QCIF)
        sizeToProfileMap.put(Size(320, 240), CamcorderProfile.QUALITY_QVGA)
        sizeToProfileMap.put(Size(352, 288), CamcorderProfile.QUALITY_CIF)
        sizeToProfileMap.put(Size(720, 480), CamcorderProfile.QUALITY_480P)
        sizeToProfileMap.put(Size(1280, 720), CamcorderProfile.QUALITY_720P)
        sizeToProfileMap.put(Size(1920, 1080), CamcorderProfile.QUALITY_1080P)
        sizeToProfileMap.put(
            Size(3840, 2160),
            CamcorderProfile.QUALITY_2160P
        )
    }


    /**
     * Returns a CamcorderProfile that's somewhat coherent with the target size,
     * to ensure we get acceptable video/audio parameters for MediaRecorders
     * (most notably the bitrate).
     *
     * @param cameraId   the camera2 id
     * @param targetSize the target video size
     * @return a profile
     */
    @JvmStatic
    fun get(cameraId: String, targetSize: Size): CamcorderProfile {
        // It seems that the way to do this is to use Integer.parseInt().
        try {
            val camera1Id = cameraId.toInt()
            return get(camera1Id, targetSize)
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            LOG.w("NumberFormatException for Camera2 id:", cameraId)
            return CamcorderProfile.get(CamcorderProfile.QUALITY_LOW)
        }
    }

    /**
     * Returns a CamcorderProfile that's somewhat coherent with the target size,
     * to ensure we get acceptable video/audio parameters for MediaRecorders
     * (most notably the bitrate).
     *
     * @param cameraId   the camera id
     * @param targetSize the target video size
     * @return a profile
     */
    @JvmStatic
    fun get(cameraId: Int, targetSize: Size): CamcorderProfile {
        val targetArea = targetSize.width.toLong() * targetSize.height
        val sizes: MutableList<Size?> = ArrayList(sizeToProfileMap.keys)
        sizes.sortWith(Comparator { s1: Size?, s2: Size? ->
            val a1 = abs(s1!!.width.toLong() * s1.height - targetArea)
            val a2 = abs(s2!!.width.toLong() * s2.height - targetArea)
            if (a1 < a2) -1 else (if (a1 == a2) 0 else 1)
        })
        while (sizes.isNotEmpty()) {
            val candidate = sizes.removeAt(0)
            val quality: Int = sizeToProfileMap[candidate]!!
            if (CamcorderProfile.hasProfile(cameraId, quality)) {
                return CamcorderProfile.get(cameraId, quality)
            }
        }
        // Should never happen, but fallback to low.
        return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW)
    }
}
