package com.base.cameraview.video.encoding

import android.media.AudioFormat

/**
 * Audio configuration to be passed as input to the constructor
 * of an [AudioMediaEncoder].
 */
class AudioConfig {
    // Not configurable options (for now)
    @JvmField
    val encoding: Int = AudioFormat.ENCODING_PCM_16BIT // Determines the sampleSizePerChannel

    // The 44.1KHz frequency is the only setting guaranteed to be available on all devices.
    // If sampleSizePerChannel changes, review noise introduction
    val sampleSizePerChannel: Int = 2 // byte/sample/channel [16bit].

    // Configurable options
    @JvmField
    var bitRate: Int = 0 // ENCODED bit rate

    @JvmField
    var channels: Int = 1

    @JvmField
    var encoder: String? = null

    @JvmField
    var mimeType: String? = "audio/mp4a-latm"

    @JvmField
    var samplingFrequency: Int = 44100 // samples/sec
    val byteRatePerChannel: Int = samplingFrequency * sampleSizePerChannel // byte/sec/channel

    fun copy(): AudioConfig {
        val config = AudioConfig()
        config.bitRate = bitRate
        config.channels = channels
        config.encoder = encoder
        config.mimeType = mimeType
        config.samplingFrequency = samplingFrequency
        return config
    }

    fun byteRate(): Int { // RAW byte rate
        return byteRatePerChannel * channels // byte/sec
    }

    @Suppress("unused")
    fun bitRate(): Int { // RAW bit rate
        return byteRate() * 8 // bit/sec
    }

    fun audioFormatChannels(): Int {
        if (channels == 1) {
            return AudioFormat.CHANNEL_IN_MONO
        } else if (channels == 2) {
            return AudioFormat.CHANNEL_IN_STEREO
        }
        throw RuntimeException("Invalid number of channels: $channels")
    }

    /**
     * We call FRAME here the chunk of data that we want to read at each loop cycle.
     *
     *
     * When this number is HIGH, the AudioRecord might be unable to keep a good pace and
     * we might end up skip some frames.
     *
     *
     * When this number is LOW, we pull a bigger number of frames and this might end up
     * delaying our recorder/encoder balance (more frames means more encoding operations).
     * In the end, this means that the recorder will skip some frames to restore the balance.
     *
     * @return the frame size
     */
    fun frameSize(): Int {
        return 1024 * channels
    }

    /**
     * Number of frames contained in the [android.media.AudioRecord] buffer.
     * In theory, the higher this value is, the safer it is to delay reading as the
     * audioRecord will hold the recorded samples anyway and return to us next time we read.
     *
     *
     * Should be coordinated with [.frameSize].
     *
     * @return the number of frames
     */
    fun audioRecordBufferFrames(): Int {
        return 50
    }

    /**
     * We allocate buffers of [.frameSize] each, which is not much.
     *
     *
     * This value indicates the maximum number of these buffers that we can allocate at a given
     * instant. This value is the number of runnable that the encoder thread is allowed to be
     * 'behind' the recorder thread. It's not safe to have it very large or we can end encoding
     * A LOT AFTER the actual recording. It's better to reduce this and skip recording at all.
     *
     *
     * Should be coordinated with [.frameSize].
     *
     * @return the buffer pool max size
     */
    fun bufferPoolMaxSize(): Int {
        return 500
    }
}
