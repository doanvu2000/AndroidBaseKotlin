package com.base.cameraview.internal

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.AudioCapabilities
import android.media.MediaCodecInfo.VideoCapabilities
import android.media.MediaCodecList
import android.media.MediaFormat
import androidx.annotation.VisibleForTesting
import com.base.cameraview.CameraLogger.Companion.create
import com.base.cameraview.size.Size
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Checks the capabilities of device encoders and adjust parameters to ensure
 * that they'll be supported by the final encoder.
 *
 *
 * Methods in this class might throw either a [VideoException] or a [AudioException].
 * Throwing this exception means that the given parameters will not be supported by the encoder
 * for that type, and cannot be tweaked to be.
 *
 *
 * When this happens, users should retry with a new [DeviceEncoders] instance, but with
 * the audio or video encoder offset incremented. This offset is the position in the encoder list
 * from which we'll choose the potential encoder.
 *
 *
 * This class will inspect the encoders list in two ways, based on the mode flag:
 *
 *
 * 1. [.MODE_RESPECT_ORDER]
 *
 *
 * Chooses the encoder as the first one that matches the given mime type.
 * This is what [MediaCodec.createEncoderByType] does,
 * and what [android.media.MediaRecorder] also does when recording.
 *
 *
 * The list is ordered based on the encoder definitions in system/etc/media_codecs.xml,
 * as explained here: [...](https://source.android.com/devices/media) , for example.
 * So taking the first means respecting the vendor priorities and should generally be
 * a good idea.
 *
 *
 * About [android.media.MediaRecorder], we know it uses this option from here:
 * [...](https://stackoverflow.com/q/57479564/4288782) where all links to source code are shown.
 * - StagefrightRecorder ([...](https://android.googlesource.com/platform/frameworks/av/+/master/media/libmediaplayerservice/StagefrightRecorder.cpp#1782))
 * - MediaCodecSource ([...](https://android.googlesource.com/platform/frameworks/av/+/master/media/libstagefright/MediaCodecSource.cpp#515))
 * - MediaCodecList ([...](https://android.googlesource.com/platform/frameworks/av/+/master/media/libstagefright/MediaCodecList.cpp#322))
 *
 *
 * To be fair, what [android.media.MediaRecorder] does is actually choose the first one
 * that configures itself without errors. We offer this option through
 * [.tryConfigureVideo] and
 * [.tryConfigureAudio].
 *
 *
 * 2. [.MODE_PREFER_HARDWARE]
 *
 *
 * This takes the list - as ordered by the vendor - and just sorts it so that hardware encoders
 * are preferred over software ones. It's questionable whether this is good or not. Some vendors
 * might forget to put hardware encoders first in the list, some others might put poor hardware
 * encoders on the bottom of the list on purpose.
 */
class DeviceEncoders @SuppressLint("NewApi") constructor(
    mode: Int, videoType: String, audioType: String, videoOffset: Int, audioOffset: Int
) {
    private val mVideoEncoder: MediaCodecInfo?
    private val mAudioEncoder: MediaCodecInfo?
    private val mVideoCapabilities: VideoCapabilities?
    private val mAudioCapabilities: AudioCapabilities?

    init {
        // We could still get a list of MediaCodecInfo for API >= 16, but it seems that the APIs
        // for querying the availability of a specified MediaFormat were only added in 21 anyway.
        if (ENABLED) {
            val encoders = this.deviceEncoders
            mVideoEncoder = findDeviceEncoder(encoders, videoType, mode, videoOffset)
            LOG.i("Enabled. Found video encoder:", mVideoEncoder.name)
            mAudioEncoder = findDeviceEncoder(encoders, audioType, mode, audioOffset)
            LOG.i("Enabled. Found audio encoder:", mAudioEncoder.name)
            mVideoCapabilities =
                mVideoEncoder.getCapabilitiesForType(videoType).videoCapabilities
            mAudioCapabilities =
                mAudioEncoder.getCapabilitiesForType(audioType).audioCapabilities
        } else {
            mVideoEncoder = null
            mAudioEncoder = null
            mVideoCapabilities = null
            mAudioCapabilities = null
            LOG.i("Disabled.")
        }
    }

    @get:VisibleForTesting
    @get:SuppressLint("NewApi")
    val deviceEncoders: MutableList<MediaCodecInfo>
        /**
         * Collects all the device encoders, which means excluding decoders.
         *
         * @return encoders
         */
        get() {
            val results = ArrayList<MediaCodecInfo>()
            val array = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
            for (info in array) {
                if (info.isEncoder) results.add(info)
            }
            return results
        }

    /**
     * Whether an encoder is a hardware encoder or not. We don't have an API to check this,
     * but we can follow what frightfulness does:
     * [...](https://android.googlesource.com/platform/frameworks/av/+/master/media/libstagefright/MediaCodecList.cpp#293)
     *
     * @param encoder encoder
     * @return true if hardware
     */
    @SuppressLint("NewApi")
    @VisibleForTesting
    fun isHardwareEncoder(encoder: String): Boolean {
        var encoder = encoder
        encoder = encoder.lowercase(Locale.getDefault())
        val isSoftwareEncoder =
            encoder.startsWith("omx.google.") || encoder.startsWith("c2.android.") || (!encoder.startsWith(
                "omx."
            ) && !encoder.startsWith("c2."))
        return !isSoftwareEncoder
    }

    /**
     * Finds the encoder we'll be using, depending on the given mode flag:
     * - [.MODE_RESPECT_ORDER] will just take the first of the list
     * - [.MODE_PREFER_HARDWARE] will prefer hardware encoders
     * Throws if we find no encoder for this type.
     *
     * @param encoders encoders
     * @param mimeType mime type
     * @param mode     mode
     * @return encoder
     */
    @SuppressLint("NewApi")
    @VisibleForTesting
    fun findDeviceEncoder(
        encoders: MutableList<MediaCodecInfo>, mimeType: String, mode: Int, offset: Int
    ): MediaCodecInfo {
        val results = ArrayList<MediaCodecInfo>()
        for (encoder in encoders) {
            val types = encoder.supportedTypes
            for (type in types) {
                if (type.equals(mimeType, ignoreCase = true)) {
                    results.add(encoder)
                    break
                }
            }
        }
        LOG.i("findDeviceEncoder -", "type:", mimeType, "encoders:", results.size)
        if (mode == MODE_PREFER_HARDWARE) {
            results.sortWith { o1, o2 ->
                val hw1 = isHardwareEncoder(o1!!.name)
                val hw2 = isHardwareEncoder(o2!!.name)
                val comparator = hw1.compareTo(hw2)
                return@sortWith comparator
            }
        }
        if (results.size < offset + 1) {
            // This should not be a VideoException or AudioException - we want the process
            // to crash here.
            throw RuntimeException("No encoders for type:$mimeType")
        }
        return results[offset]
    }

    /**
     * Returns a video size supported by the device encoders.
     * Throws if input width or height are out of the supported boundaries.
     *
     * @param size input size
     * @return adjusted size
     */
    @SuppressLint("NewApi")
    fun getSupportedVideoSize(size: Size): Size {
        if (!ENABLED) return size
        var width = size.width
        var height = size.height
        val aspect = width.toDouble() / height
        LOG.i("getSupportedVideoSize - started. width:", width, "height:", height)

        // If width is too large, scale down, but keep aspect ratio.
        if (mVideoCapabilities!!.supportedWidths.getUpper() < width) {
            width = mVideoCapabilities.supportedWidths.getUpper()
            height = (width / aspect).roundToInt()
            LOG.i(
                "getSupportedVideoSize - exceeds maxWidth! width:", width, "height:", height
            )
        }

        // If height is too large, scale down, but keep aspect ratio.
        if (mVideoCapabilities.supportedHeights.getUpper() < height) {
            height = mVideoCapabilities.supportedHeights.getUpper()
            width = (aspect * height).roundToInt()
            LOG.i(
                "getSupportedVideoSize - exceeds maxHeight! width:", width, "height:", height
            )
        }

        // Adjust the alignment.
        while (width % mVideoCapabilities.widthAlignment != 0) width--
        while (height % mVideoCapabilities.heightAlignment != 0) height--
        LOG.i("getSupportedVideoSize - aligned. width:", width, "height:", height)

        // It's still possible that we're BELOW the lower.
        if (!mVideoCapabilities.supportedWidths.contains(width)) {
            throw VideoException(
                "Width not supported after adjustment." + " Desired:" + width + " Range:" + mVideoCapabilities.supportedWidths
            )
        }
        if (!mVideoCapabilities.supportedHeights.contains(height)) {
            throw VideoException(
                "Height not supported after adjustment." + " Desired:" + height + " Range:" + mVideoCapabilities.supportedHeights
            )
        }

        // We cannot change the aspect ratio, but the max block count might also be the
        // issue. Try to find a width that contains a height that would accept our AR.
        try {
            if (!mVideoCapabilities.getSupportedHeightsFor(width).contains(height)) {
                var candidateWidth = width
                val minWidth = mVideoCapabilities.supportedWidths.getLower()
                val widthAlignment = mVideoCapabilities.widthAlignment
                while (candidateWidth >= minWidth) {
                    // Reduce by 32 and realign just in case, then check if our AR is now
                    // supported. If it is, restart from scratch to go through the other checks.
                    candidateWidth -= 32
                    while (candidateWidth % widthAlignment != 0) candidateWidth--
                    val candidateHeight = (candidateWidth / aspect).roundToInt()
                    if (mVideoCapabilities.getSupportedHeightsFor(candidateWidth)
                            .contains(candidateHeight)
                    ) {
                        LOG.w("getSupportedVideoSize - restarting with smaller size.")
                        return getSupportedVideoSize(Size(candidateWidth, candidateHeight))
                    }
                }
            }
        } catch (ignore: IllegalArgumentException) {
            ignore.printStackTrace()
        }

        // It's still possible that we're unsupported for other reasons.
        if (!mVideoCapabilities.isSizeSupported(width, height)) {
            throw VideoException(
                "Size not supported for unknown reason." + " Might be an aspect ratio issue." + " Desired size:" + Size(
                    width,
                    height
                )
            )
        }
        return Size(width, height)
    }

    /**
     * Returns a video bit rate supported by the device encoders.
     * This means adjusting the input bit rate if needed, to match encoder constraints.
     *
     * @param bitRate input rate
     * @return adjusted rate
     */
    @SuppressLint("NewApi")
    fun getSupportedVideoBitRate(bitRate: Int): Int {
        if (!ENABLED) return bitRate
        val newBitRate = mVideoCapabilities!!.bitrateRange.clamp(bitRate)
        LOG.i(
            "getSupportedVideoBitRate -", "inputRate:", bitRate, "adjustedRate:", newBitRate
        )
        return newBitRate
    }

    /**
     * Returns a video frame rate supported by the device encoders.
     * This means adjusting the input frame rate if needed, to match encoder constraints.
     *
     * @param frameRate input rate
     * @return adjusted rate
     */
    @SuppressLint("NewApi")
    fun getSupportedVideoFrameRate(size: Size, frameRate: Int): Int {
        if (!ENABLED) return frameRate
        val newFrameRate =
            (mVideoCapabilities!!.getSupportedFrameRatesFor(size.width, size.height)
                .clamp(frameRate.toDouble()) as Double).toInt()
        LOG.i(
            "getSupportedVideoFrameRate -", "inputRate:", frameRate, "adjustedRate:", newFrameRate
        )
        return newFrameRate
    }

    /**
     * Returns an audio bit rate supported by the device encoders.
     * This means adjusting the input bit rate if needed, to match encoder constraints.
     *
     * @param bitRate input rate
     * @return adjusted rate
     */
    @SuppressLint("NewApi")
    fun getSupportedAudioBitRate(bitRate: Int): Int {
        if (!ENABLED) return bitRate
        val newBitRate = mAudioCapabilities!!.bitrateRange.clamp(bitRate)
        LOG.i(
            "getSupportedAudioBitRate -", "inputRate:", bitRate, "adjustedRate:", newBitRate
        )
        return newBitRate
    }

    @get:SuppressLint("NewApi")
    val videoEncoder: String?
        /**
         * Returns the name of the video encoder if we were able to determine one.
         *
         * @return encoder name
         */
        get() {
            return mVideoEncoder?.name
        }

    @get:SuppressLint("NewApi")
    val audioEncoder: String?
        /**
         * Returns the name of the audio encoder if we were able to determine one.
         *
         * @return encoder name
         */
        get() {
            return mAudioEncoder?.name
        }


    // Won't do this for audio sample rate. As far as I remember, the value we're using,
    // 44.1kHz, is guaranteed to be available, and it's not configurable.
    @SuppressLint("NewApi")
    fun tryConfigureVideo(
        mimeType: String, size: Size, frameRate: Int, bitRate: Int
    ) {
        if (mVideoEncoder != null) {
            var codec: MediaCodec? = null
            try {
                val format = MediaFormat.createVideoFormat(
                    mimeType, size.width, size.height
                )
                format.setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                )
                format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                codec = MediaCodec.createByCodecName(mVideoEncoder.name)
                codec.configure(
                    format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE
                )
            } catch (e: Exception) {
                throw VideoException("Failed to configure video codec: " + e.message)
            } finally {
                if (codec != null) {
                    try {
                        codec.release()
                    } catch (ignore: Exception) {
                        ignore.printStackTrace()
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    fun tryConfigureAudio(
        mimeType: String, bitRate: Int, sampleRate: Int, channels: Int
    ) {
        if (mAudioEncoder != null) {
            var codec: MediaCodec? = null
            try {
                val format = MediaFormat.createAudioFormat(
                    mimeType, sampleRate, channels
                )
                val channelMask = if (channels == 2) AudioFormat.CHANNEL_IN_STEREO
                else AudioFormat.CHANNEL_IN_MONO
                format.setInteger(MediaFormat.KEY_CHANNEL_MASK, channelMask)
                format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)

                codec = MediaCodec.createByCodecName(mAudioEncoder.name)
                codec.configure(
                    format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE
                )
            } catch (e: Exception) {
                throw AudioException("Failed to configure video audio: " + e.message)
            } finally {
                if (codec != null) {
                    try {
                        codec.release()
                    } catch (ignore: Exception) {
                        ignore.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * Exception thrown when trying to find appropriate values
     * for a video encoder.
     */
    class VideoException(message: String) : RuntimeException(message)

    /**
     * Exception thrown when trying to find appropriate values
     * for an audio encoder. Currently never thrown.
     */
    class AudioException(message: String) : RuntimeException(message)

    companion object {
        const val MODE_RESPECT_ORDER: Int = 0
        const val MODE_PREFER_HARDWARE: Int = 1
        private val TAG: String = DeviceEncoders::class.java.simpleName
        private val LOG = create(TAG)

        @VisibleForTesting
        var ENABLED: Boolean = true
    }
}
