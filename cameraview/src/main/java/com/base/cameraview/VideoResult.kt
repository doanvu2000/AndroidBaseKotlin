package com.base.cameraview

import android.location.Location
import com.base.cameraview.controls.Audio
import com.base.cameraview.controls.AudioCodec
import com.base.cameraview.controls.Facing
import com.base.cameraview.controls.VideoCodec
import com.base.cameraview.size.Size
import java.io.File
import java.io.FileDescriptor

/**
 * Wraps the result of a video recording started by [CameraView.takeVideo].
 */
class VideoResult internal constructor(builder: Stub) {
    /**
     * Returns whether this result comes from a snapshot.
     *
     * @return whether this is a snapshot
     */
    val isSnapshot: Boolean = builder.isSnapshot

    /**
     * Returns geographic information for this video, if any.
     * If it was set, it is also present in the file metadata.
     *
     * @return a nullable Location
     */
    val location: Location? = builder.location

    /**
     * Returns the clock-wise rotation that should be applied to the
     * video frames before displaying. If it is non-zero, it is also present
     * in the video metadata, so most reader will take care of it.
     *
     * @return the clock-wise rotation
     */
    val rotation: Int = builder.rotation

    /**
     * Returns the size of the frames after the rotation is applied.
     *
     * @return the Size of this video
     */
    val size: Size = builder.size
    private val file: File = builder.file!!
    private val fileDescriptor: FileDescriptor = builder.fileDescriptor!!

    /**
     * Returns the facing value with which this video was recorded.
     *
     * @return the Facing of this video
     */
    val facing: Facing = builder.facing!!

    /**
     * Returns the codec that was used to encode the video frames.
     *
     * @return the video codec
     */
    val videoCodec: VideoCodec = builder.videoCodec

    /**
     * Returns the codec that was used to encode the audio frames.
     *
     * @return the audio codec
     */
    val audioCodec: AudioCodec = builder.audioCodec!!

    /**
     * Returns the [Audio] setting for this video.
     *
     * @return the audio setting for this video
     */
    val audio: Audio = builder.audio!!

    /**
     * Returns the max file size in bytes that was set before recording,
     * or 0 if no constraint was set.
     *
     * @return the max file size in bytes
     */
    val maxSize: Long = builder.maxSize

    /**
     * Returns the max video duration in milliseconds that was set before recording,
     * or 0 if no constraint was set.
     *
     * @return the max duration in milliseconds
     */
    val maxDuration: Int = builder.maxDuration

    /**
     * Returns the reason why the recording was stopped.
     *
     * @return one of [.REASON_USER], [.REASON_MAX_DURATION_REACHED]
     * or [.REASON_MAX_SIZE_REACHED].
     */
    val terminationReason: Int = builder.endReason

    /**
     * Returns the bit rate used for video encoding.
     *
     * @return the video bit rate
     */
    val videoBitRate: Int = builder.videoBitRate

    /**
     * Returns the frame rate used for video encoding
     * in frames per second.
     *
     * @return the video frame rate
     */
    val videoFrameRate: Int = builder.videoFrameRate

    /**
     * Returns the bit rate used for audio encoding.
     *
     * @return the audio bit rate
     */
    val audioBitRate: Int = builder.audioBitRate

    /**
     * Returns the file where the video was saved.
     *
     * @return the File of this video
     */
    fun getFile(): File {
        return file
    }

    /**
     * Returns the file descriptor where the video was saved.
     *
     * @return the File Descriptor of this video
     */
    fun getFileDescriptor(): FileDescriptor {
        return fileDescriptor
    }

    /**
     * A result stub, for internal use only.
     */
    class Stub internal constructor() {
        var isSnapshot: Boolean = false
        var location: Location? = null

        @JvmField
        var rotation: Int = 0

        @JvmField
        var size: Size = Size.defaultSize()

        @JvmField
        var file: File? = null
        var fileDescriptor: FileDescriptor? = null
        var facing: Facing? = null

        @JvmField
        var videoCodec: VideoCodec = VideoCodec.DEFAULT

        @JvmField
        var audioCodec: AudioCodec = AudioCodec.DEFAULT

        @JvmField
        var audio: Audio? = null

        @JvmField
        var maxSize: Long = 0

        @JvmField
        var maxDuration: Int = 0

        @JvmField
        var endReason: Int = 0

        @JvmField
        var videoBitRate: Int = 0

        @JvmField
        var videoFrameRate: Int = 0

        @JvmField
        var audioBitRate: Int = 0
    }

    companion object {
        @Suppress("unused")
        const val REASON_USER: Int = 0
        const val REASON_MAX_SIZE_REACHED: Int = 1
        const val REASON_MAX_DURATION_REACHED: Int = 2
    }
}
