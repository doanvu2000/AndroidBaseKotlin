package com.base.cameraview.video.encoding

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Random
import kotlin.math.sin

/**
 * An AudioNoise instance offers buffers of noise that we can use when recording
 * some samples failed for some reason.
 *
 *
 * Since we can't create noise anytime it's needed - that would be expensive and
 * slow down the recording thread - we create a big noise buffer at start time.
 *
 *
 * We'd like to work with [java.nio.ShortBuffer]s, but this requires converting the
 * input buffer to ShortBuffer each time, and this can be expensive.
 */
internal class AudioNoise(config: AudioConfig) {
    private val mNoiseBuffer: ByteBuffer

    init {
        require(config.sampleSizePerChannel == 2) { "AudioNoise expects 2bytes-1short samples." }
        mNoiseBuffer = ByteBuffer
            .allocateDirect(config.frameSize() * FRAMES)
            .order(ByteOrder.nativeOrder())
        var i = 0.0
        val frequency = config.frameSize() / 2.0 // each X samples, the signal repeats
        val step = Math.PI / frequency // the increase in radians
        val max = 10.0 // might choose this from 0 to Short.MAX_VALUE
        while (mNoiseBuffer.hasRemaining()) {
            val noise = (sin(++i * step) * max).toInt().toShort()
            mNoiseBuffer.put(noise.toByte())
            mNoiseBuffer.put((noise.toInt() shr 8).toByte())
        }
        mNoiseBuffer.rewind()
    }

    fun fill(outBuffer: ByteBuffer) {
        mNoiseBuffer.clear()
        if (mNoiseBuffer.capacity() == outBuffer.remaining()) {
            mNoiseBuffer.position(0) // Happens if FRAMES = 1.
        } else {
            mNoiseBuffer.position(
                RANDOM.nextInt(
                    mNoiseBuffer.capacity()
                            - outBuffer.remaining()
                )
            )
        }
        mNoiseBuffer.limit(mNoiseBuffer.position() + outBuffer.remaining())
        outBuffer.put(mNoiseBuffer)
    }

    companion object {
        private const val FRAMES = 1 // After testing, it looks like this is the best setup
        private val RANDOM = Random()
    }
}
