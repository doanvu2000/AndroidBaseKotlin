package com.example.baseproject.base.ui.camerademo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.base.cameraview.CameraException
import com.base.cameraview.CameraListener
import com.base.cameraview.CameraOptions
import com.base.cameraview.PictureResult
import com.base.cameraview.VideoResult
import com.base.cameraview.controls.Mode
import com.base.cameraview.frame.Frame
import com.base.cameraview.frame.FrameProcessor
import com.example.baseproject.base.base_view.screen.BaseActivity
import com.example.baseproject.base.utils.extension.loadSrc
import com.example.baseproject.base.utils.util.AppLogger
import com.example.baseproject.databinding.ActivityCameraViewLibDemoBinding

class CameraViewLibDemoActivity : BaseActivity<ActivityCameraViewLibDemoBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityCameraViewLibDemoBinding {
        return ActivityCameraViewLibDemoBinding.inflate(inflater)
    }

    private fun getPermissionList(): MutableList<String> {
        val permission = mutableListOf<String>()
        permission.add(Manifest.permission.CAMERA)
        permission.add(Manifest.permission.RECORD_AUDIO)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            permission.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        return permission
    }

    override fun initView() {
        //todo: if i have free-time, i will be code
        if (hasCameraAndRecordPermissions()) {
            //show camera
            setupCamera()
        } else {
            requestPermissionLauncher.launch(getPermissionList().toTypedArray())
        }
    }

    override fun initData() {

    }

    override fun initListener() {
        binding.btnBack.clickSafe(isAnimationClick = true) {
            onBack()
        }

        binding.btnSwitchCamera.clickSafe(isAnimationClick = true) {
            switchCamera()
        }
    }

    private fun switchCamera() {
        showDialogInitCamera()
//        binding.cameraView.toggleFacing()
        binding.cameraView.takePicture()
        delayToAction(300L) {
            hideDialogInitCamera()
        }
    }

    private fun setupCamera() {
        showDialogInitCamera()
        binding.cameraView.apply {
            setLifecycleOwner(this@CameraViewLibDemoActivity)
            clearCameraListeners()
            addCameraListener(cameraListener)
            mode = Mode.PICTURE
            addFrameProcessor(frameProcessor)
        }
    }

    //region dialogInitCamera
    private val dialogInitCamera by lazy {
        DialogInitializeCamera(this)
    }

    private fun showDialogInitCamera() {
        launchCoroutineMain {
            dialogInitCamera.show()
        }
    }

    private fun hideDialogInitCamera() {
        launchCoroutineMain {
            dialogInitCamera.hide()
        }
    }
    //endregion

    private val frameProcessor = object : FrameProcessor {
        override fun process(frame: Frame) {
        }
    }

    private val cameraListener = object : CameraListener() {
        override fun onCameraOpened(options: CameraOptions) {
            super.onCameraOpened(options)
            AppLogger.i(TAG, "onCameraOpened")
            hideDialogInitCamera()
        }

        override fun onCameraClosed() {
            super.onCameraClosed()
            AppLogger.e(TAG, "onCameraClosed")
            hideDialogInitCamera()
        }

        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
            AppLogger.e(TAG, "onCameraError")
            hideDialogInitCamera()
        }

        override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()
            AppLogger.d(TAG, "onVideoRecordingStart")
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
            AppLogger.w(TAG, "onVideoRecordingEnd")
        }

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            AppLogger.i(TAG, "onVideoTaken")
        }

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            AppLogger.i(TAG, "onPictureTaken: $result")
            binding.imgResult.loadSrc(result.data)
        }

        override fun onPictureShutter() {
            super.onPictureShutter()
            AppLogger.w(TAG, "onPictureShutter")
        }
    }

    //region request permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (hasCameraAndRecordPermissions()) {
            //show camera
            setupCamera()
        }
    }

    /** Convenience method used to check if all permissions required by this app are granted */
    fun Context.hasCameraAndRecordPermissions() = getPermissionList().all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
    //endregion
}