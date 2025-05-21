package com.base.cameraview.video

import android.media.CamcorderProfile
import android.media.MediaRecorder
import com.base.cameraview.CameraLogger
import com.base.cameraview.CameraLogger.Companion.create
import com.base.cameraview.VideoResult
import com.base.cameraview.controls.Audio
import com.base.cameraview.controls.AudioCodec
import com.base.cameraview.controls.VideoCodec
import com.base.cameraview.internal.DeviceEncoders
import com.base.cameraview.internal.DeviceEncoders.AudioException
import com.base.cameraview.internal.DeviceEncoders.VideoException
import com.base.cameraview.size.Size
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * A [VideoRecorder] that uses [MediaRecorder] APIs.
 *
 *
 * When started, the media recorder will be prepared in
 * [.prepareMediaRecorder]. This will call two abstract methods:
 * - [.getCamcorderProfile]
 * - [.applyVideoSource]
 *
 *
 * Subclasses can also call [.prepareMediaRecorder] before start happens,
 * in which case it will not be prepared twice. This can be used for example to test some
 * configurations.
 */
abstract class FullVideoRecorder internal constructor(listener: VideoResultListener?) :
    VideoRecorder(listener) {
    protected var mMediaRecorder: MediaRecorder? = null
    private var mProfile: CamcorderProfile? = null
    private var mMediaRecorderPrepared = false


    /**
     * Subclasses should return an appropriate CamcorderProfile.
     * This could be taken from the [com.base.cameraview.internal.CamcorderProfiles] utility class based on the
     * stub declared size, for instance.
     *
     * @param stub the stub
     * @return the profile
     */
    protected abstract fun getCamcorderProfile(stub: VideoResult.Stub): CamcorderProfile

    /**
     * Subclasses should apply a video source to the given recorder.
     *
     * @param stub          the stub
     * @param mediaRecorder the recorder
     */
    protected abstract fun applyVideoSource(
        stub: VideoResult.Stub, mediaRecorder: MediaRecorder
    )

    protected fun prepareMediaRecorder(stub: VideoResult.Stub): Boolean {
        if (mMediaRecorderPrepared) return true
        // We kind of trust the stub size at this point. It's coming from CameraOptions sizes
        // and it's clipped to be less than CamcorderProfile's highest available profile.
        // However, we still can't trust the developer parameters (e.g. bit rates), and even
        // without them, the camera declared sizes can cause crashes in MediaRecorder (#467, #602).
        // A possible solution was to prepare without checking DeviceEncoders first, and should it
        // fail, prepare again checking them. However, when parameters are wrong, MediaRecorder
        // fails on start() instead of prepare() (start failed -19), so this wouldn't be effective.
        return prepareMediaRecorder(stub, true)
    }

    private fun prepareMediaRecorder(
        stub: VideoResult.Stub, applyEncodersConstraints: Boolean
    ): Boolean {
        LOG.i("prepareMediaRecorder:", "Preparing on thread", Thread.currentThread())
        // 1. Create reference and ask for the CamcorderProfile
        @Suppress("DEPRECATION")
        mMediaRecorder = MediaRecorder()
        mProfile = getCamcorderProfile(stub)

        // 2. Set the video and audio sources.
        applyVideoSource(stub, mMediaRecorder!!)
        val audioChannels = when (stub.audio) {
            Audio.OFF -> {
                0
            }

            Audio.ON -> {
                mProfile!!.audioChannels
            }

            Audio.MONO -> {
                1
            }

            Audio.STEREO -> {
                2
            }
        }

        val hasAudio = audioChannels > 0

        if (hasAudio) {
            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        }

        // 3. Set the output format. Before, change the profile data if the user
        // has specified a specific codec.
        if (stub.videoCodec == VideoCodec.H_264) {
            mProfile!!.videoCodec = MediaRecorder.VideoEncoder.H264
            mProfile!!.fileFormat = MediaRecorder.OutputFormat.MPEG_4
        } else if (stub.videoCodec == VideoCodec.H_263) {
            mProfile!!.videoCodec = MediaRecorder.VideoEncoder.H263
            mProfile!!.fileFormat = MediaRecorder.OutputFormat.MPEG_4 // should work
        }
        // Set audio codec if the user has specified a specific codec.
        when (stub.audioCodec) {
            AudioCodec.DEVICE_DEFAULT -> {

            }

            AudioCodec.AAC -> {
                mProfile!!.audioCodec = MediaRecorder.AudioEncoder.AAC
            }

            AudioCodec.HE_AAC -> {
                mProfile!!.audioCodec = MediaRecorder.AudioEncoder.HE_AAC
            }

            AudioCodec.AAC_ELD -> {
                mProfile!!.audioCodec = MediaRecorder.AudioEncoder.AAC_ELD
            }
        }

        mMediaRecorder!!.setOutputFormat(mProfile!!.fileFormat)

        // 4. Update the VideoResult stub with information from the profile, if the
        // stub values are absent or incomplete
        if (stub.videoFrameRate <= 0) stub.videoFrameRate = mProfile!!.videoFrameRate
        if (stub.videoBitRate <= 0) stub.videoBitRate = mProfile!!.videoBitRate
        if (stub.audioBitRate <= 0 && hasAudio) stub.audioBitRate = mProfile!!.audioBitRate

        // 5. Update the VideoResult stub with DeviceEncoders constraints
        if (applyEncodersConstraints) {
            // A. Get the audio mime type
            // https://android.googlesource.com/platform/frameworks/av/+/master/media/libmediaplayerservice/StagefrightRecorder.cpp#1096
            // https://github.com/MrAlex94/Waterfox-Old/blob/master/media/libstagefright/frameworks/av/media/libstagefright/MediaDefs.cpp
            val audioType = when (mProfile!!.audioCodec) {
                MediaRecorder.AudioEncoder.AMR_NB -> "audio/3gpp"
                MediaRecorder.AudioEncoder.AMR_WB -> "audio/amr-wb"
                MediaRecorder.AudioEncoder.AAC, MediaRecorder.AudioEncoder.HE_AAC, MediaRecorder.AudioEncoder.AAC_ELD -> "audio/mp4a-latm"

                MediaRecorder.AudioEncoder.VORBIS -> "audio/vorbis"
                MediaRecorder.AudioEncoder.DEFAULT -> "audio/3gpp"
                else -> "audio/3gpp"
            }
            // B. Get the video mime type
            // https://android.googlesource.com/platform/frameworks/av/+/master/media/libmediaplayerservice/StagefrightRecorder.cpp#1650
            // https://github.com/MrAlex94/Waterfox-Old/blob/master/media/libstagefright/frameworks/av/media/libstagefright/MediaDefs.cpp
            val videoType = when (mProfile!!.videoCodec) {
                MediaRecorder.VideoEncoder.H263 -> "video/3gpp"
                MediaRecorder.VideoEncoder.H264 -> "video/avc"
                MediaRecorder.VideoEncoder.MPEG_4_SP -> "video/mp4v-es"
                MediaRecorder.VideoEncoder.VP8 -> "video/x-vnd.on2.vp8"
                MediaRecorder.VideoEncoder.HEVC -> "video/hevc"
                MediaRecorder.VideoEncoder.DEFAULT -> "video/avc"
                else -> "video/avc"
            }
            // C. Check DeviceEncoders support
            val flip = stub.rotation % 180 != 0
            if (flip) stub.size = stub.size.flip()
            var newVideoSize: Size? = null
            var newVideoBitRate = 0
            var newAudioBitRate = 0
            var newVideoFrameRate = 0
            var videoEncoderOffset = 0
            var audioEncoderOffset = 0
            var encodersFound = false
            while (!encodersFound) {
                LOG.i(
                    "prepareMediaRecorder:",
                    "Checking DeviceEncoders...",
                    "videoOffset:",
                    videoEncoderOffset,
                    "audioOffset:",
                    audioEncoderOffset
                )
                val encoders: DeviceEncoders?
                try {
                    encoders = DeviceEncoders(
                        DeviceEncoders.MODE_RESPECT_ORDER,
                        videoType,
                        audioType,
                        videoEncoderOffset,
                        audioEncoderOffset
                    )
                } catch (e: RuntimeException) {
                    e.printStackTrace()
                    LOG.w(
                        "prepareMediaRecorder:",
                        "Could not respect encoders parameters.",
                        "Trying again without checking encoders."
                    )
                    return prepareMediaRecorder(stub, false)
                }
                try {
                    newVideoSize = encoders.getSupportedVideoSize(stub.size)
                    newVideoBitRate = encoders.getSupportedVideoBitRate(stub.videoBitRate)
                    newVideoFrameRate = encoders.getSupportedVideoFrameRate(
                        newVideoSize, stub.videoFrameRate
                    )
                    encoders.tryConfigureVideo(
                        videoType, newVideoSize, newVideoFrameRate, newVideoBitRate
                    )
                    if (hasAudio) {
                        newAudioBitRate = encoders.getSupportedAudioBitRate(stub.audioBitRate)
                        encoders.tryConfigureAudio(
                            audioType, newAudioBitRate, mProfile!!.audioSampleRate, audioChannels
                        )
                    }
                    encodersFound = true
                } catch (videoException: VideoException) {
                    LOG.i(
                        "prepareMediaRecorder:", "Got VideoException:", videoException.message!!
                    )
                    videoEncoderOffset++
                } catch (audioException: AudioException) {
                    LOG.i(
                        "prepareMediaRecorder:", "Got AudioException:", audioException.message!!
                    )
                    audioEncoderOffset++
                }
            }
            // D. Apply results
            stub.size = newVideoSize
            stub.videoBitRate = newVideoBitRate
            stub.audioBitRate = newAudioBitRate
            stub.videoFrameRate = newVideoFrameRate
            if (flip) stub.size = stub.size.flip()
        }

        // 6A. Configure MediaRecorder from stub and from profile (video)
        val flip = stub.rotation % 180 != 0
        mMediaRecorder!!.setVideoSize(
            if (flip) stub.size.height else stub.size.width,
            if (flip) stub.size.width else stub.size.height
        )
        mMediaRecorder!!.setVideoFrameRate(stub.videoFrameRate)
        mMediaRecorder!!.setVideoEncoder(mProfile!!.videoCodec)
        mMediaRecorder!!.setVideoEncodingBitRate(stub.videoBitRate)

        // 6B. Configure MediaRecorder from stub and from profile (audio)
        if (hasAudio) {
            mMediaRecorder!!.setAudioChannels(audioChannels)
            mMediaRecorder!!.setAudioSamplingRate(mProfile!!.audioSampleRate)
            mMediaRecorder!!.setAudioEncoder(mProfile!!.audioCodec)
            mMediaRecorder!!.setAudioEncodingBitRate(stub.audioBitRate)
        }

        // 7. Set other params
        if (stub.location != null) {
            mMediaRecorder!!.setLocation(
                stub.location.latitude.toFloat(), stub.location.longitude.toFloat()
            )
        }

        if (stub.file != null) {
            mMediaRecorder!!.setOutputFile(stub.file.absolutePath)
        } else if (stub.fileDescriptor != null) {
            mMediaRecorder!!.setOutputFile(stub.fileDescriptor)
        } else {
            throw IllegalStateException("file and fileDescriptor are both null.")
        }

        mMediaRecorder!!.setOrientationHint(stub.rotation)
        // When using MEDIA_RECORDER_INFO_MAX_FILE_SIZE_REACHED, the recorder might have stopped
        // before calling it. But this creates issues on Camera2 Legacy devices - they need a
        // callback BEFORE the recorder stops (see Camera2Engine). For this reason, we increase
        // the max size and use MEDIA_RECORDER_INFO_MAX_FILE_SIZE_APPROACHING instead.
        // Would do this with max duration as well but there's no such callback.
        val maxFileSize = if (stub.maxSize <= 0) {
            stub.maxSize
        } else {
            (stub.maxSize / 0.9f).roundToLong()
        }

        mMediaRecorder!!.setMaxFileSize(maxFileSize)

        LOG.i(
            "prepareMediaRecorder:",
            "Increased max size from",
            stub.maxSize,
            "to",
            (stub.maxSize / 0.9f).roundToInt()
        )
        mMediaRecorder!!.setMaxDuration(stub.maxDuration)
        mMediaRecorder!!.setOnInfoListener { mediaRecorder, what, extra ->
            LOG.i(
                "OnInfoListener:", "Received info", what, extra, "Thread: ", Thread.currentThread()
            )
            var shouldStop = false
            when (what) {
                MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED -> {
                    mResult.endReason = VideoResult.REASON_MAX_DURATION_REACHED
                    shouldStop = true
                }

                MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING, MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED -> {
                    // On rare occasions APPROACHING is not called. Make sure we listen to
                    // REACHED as well.
                    mResult.endReason = VideoResult.REASON_MAX_SIZE_REACHED
                    shouldStop = true
                }
            }
            if (shouldStop) {
                LOG.i("OnInfoListener:", "Stopping")
                stop(false)
            }
        }
        mMediaRecorder!!.setOnErrorListener { mr: MediaRecorder?, what: Int, extra: Int ->
            LOG.e("OnErrorListener: got error", what, extra, ". Stopping.")
            mResult = null
            mError = RuntimeException("MediaRecorder error: $what $extra")
            LOG.i("OnErrorListener:", "Stopping")
            stop(false)
        }

        // 8. Prepare the Recorder
        try {
            mMediaRecorder!!.prepare()
            mMediaRecorderPrepared = true
            mError = null
            return true
        } catch (e: Exception) {
            LOG.w("prepareMediaRecorder:", "Error while preparing media recorder.", e)
            mMediaRecorderPrepared = false
            mError = e
            return false
        }
    }

    override fun onStart() {
        if (!prepareMediaRecorder(mResult)) {
            mResult = null
            stop(false)
            return
        }

        try {
            mMediaRecorder!!.start()
            dispatchVideoRecordingStart()
        } catch (e: Exception) {
            LOG.w("start:", "Error while starting media recorder.", e)
            mResult = null
            mError = e
            stop(false)
        }
    }

    override fun onStop(isCameraShutdown: Boolean) {
        if (mMediaRecorder != null) {
            dispatchVideoRecordingEnd()
            try {
                LOG.i("stop:", "Stopping MediaRecorder...")
                // TODO HANGS (rare, emulator only)
                mMediaRecorder!!.stop()
                LOG.i("stop:", "Stopped MediaRecorder.")
            } catch (e: Exception) {
                // This can happen if stopVideo() is called right after takeVideo()
                // (in which case we don't care). Or when prepare()/start() have failed for
                // some reason and we are not allowed to call stop.
                // Make sure we don't override the error if one exists already.
                mResult = null
                if (mError == null) {
                    LOG.w("stop:", "Error while closing media recorder.", e)
                    mError = e
                }
            }
            try {
                LOG.i("stop:", "Releasing MediaRecorder...")
                mMediaRecorder!!.release()
                LOG.i("stop:", "Released MediaRecorder.")
            } catch (e: Exception) {
                mResult = null
                if (mError == null) {
                    LOG.w("stop:", "Error while releasing media recorder.", e)
                    mError = e
                }
            }
        }
        mProfile = null
        mMediaRecorder = null
        mMediaRecorderPrepared = false
        dispatchResult()
    }

    companion object {
        private val TAG: String = FullVideoRecorder::class.java.simpleName
        val LOG: CameraLogger = create(TAG)
    }
}
