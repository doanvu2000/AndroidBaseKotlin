package com.base.cameraview.controls

import android.content.Context
import android.content.res.TypedArray
import com.base.cameraview.R
import com.base.cameraview.controls.Facing.Companion.defaultFacing

/**
 * Parses controls from XML attributes.
 */
class ControlParser(context: Context, array: TypedArray) {
    private val preview: Int =
        array.getInteger(R.styleable.CameraView_cameraPreview, Preview.DEFAULT.value)
    private val facing: Int = array.getInteger(
        R.styleable.CameraView_cameraFacing,
        defaultFacing(context).value
    )
    private val flash: Int =
        array.getInteger(R.styleable.CameraView_cameraFlash, Flash.DEFAULT.value)
    private val grid: Int = array.getInteger(R.styleable.CameraView_cameraGrid, Grid.DEFAULT.value)
    private val whiteBalance: Int = array.getInteger(
        R.styleable.CameraView_cameraWhiteBalance,
        WhiteBalance.DEFAULT.value
    )
    private val mode: Int = array.getInteger(R.styleable.CameraView_cameraMode, Mode.DEFAULT.value)
    private val hdr: Int = array.getInteger(R.styleable.CameraView_cameraHdr, Hdr.DEFAULT.value)
    private val audio: Int =
        array.getInteger(R.styleable.CameraView_cameraAudio, Audio.DEFAULT.value)
    private val videoCodec: Int = array.getInteger(
        R.styleable.CameraView_cameraVideoCodec,
        VideoCodec.DEFAULT.value
    )
    private val audioCodec: Int = array.getInteger(
        R.styleable.CameraView_cameraAudioCodec,
        AudioCodec.DEFAULT.value
    )
    private val engine: Int =
        array.getInteger(R.styleable.CameraView_cameraEngine, Engine.DEFAULT.value)
    private val pictureFormat: Int = array.getInteger(
        R.styleable.CameraView_cameraPictureFormat,
        PictureFormat.DEFAULT.value
    )

    fun getPreview(): Preview {
        return Preview.fromValue(preview)
    }

    fun getFacing(): Facing {
        return Facing.fromValue(facing)!!
    }

    fun getFlash(): Flash {
        return Flash.fromValue(flash)
    }

    fun getGrid(): Grid {
        return Grid.fromValue(grid)
    }

    fun getMode(): Mode {
        return Mode.fromValue(mode)
    }

    fun getWhiteBalance(): WhiteBalance {
        return WhiteBalance.fromValue(whiteBalance)
    }

    fun getHdr(): Hdr {
        return Hdr.fromValue(hdr)
    }

    fun getAudio(): Audio {
        return Audio.fromValue(audio)
    }

    fun getAudioCodec(): AudioCodec {
        return AudioCodec.fromValue(audioCodec)
    }

    fun getVideoCodec(): VideoCodec {
        return VideoCodec.fromValue(videoCodec)
    }

    fun getEngine(): Engine {
        return Engine.fromValue(engine)
    }

    fun getPictureFormat(): PictureFormat {
        return PictureFormat.fromValue(pictureFormat)
    }
}
