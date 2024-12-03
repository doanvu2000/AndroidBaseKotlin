package com.example.baseproject.base.ui.pick_image

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import com.example.baseproject.base.base_view.BaseActivity
import com.example.baseproject.base.dialog.DialogNeedPermission
import com.example.baseproject.base.utils.extension.CAMERA_PERMISSION
import com.example.baseproject.base.utils.extension.createImageFile
import com.example.baseproject.base.utils.extension.getOrientationImage
import com.example.baseproject.base.utils.extension.getUriByFileProvider
import com.example.baseproject.base.utils.extension.hasCameraPermission
import com.example.baseproject.base.utils.extension.isNeedRotateOrFlipImage
import com.example.baseproject.base.utils.extension.loadSrcNoCache
import com.example.baseproject.base.utils.extension.modifyOrientation
import com.example.baseproject.base.utils.util.Constants
import com.example.baseproject.base.utils.util.delayResetFlagPermission
import com.example.baseproject.databinding.ActivityTakePictureBinding
import java.io.FileNotFoundException

class TakePictureActivity : BaseActivity<ActivityTakePictureBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityTakePictureBinding {
        return ActivityTakePictureBinding.inflate(inflater)
    }

    //region variable
    private var uriPhoto: Uri? = null
    private var originalBitmap: Bitmap? = null
    private var orientationUri: Int? = null

    private val dialogNeedPermission by lazy {
        DialogNeedPermission(this)
    }
    //endregion

    override fun initView() {

    }

    override fun initData() {

    }

    override fun initListener() {
        binding.btnBack.clickSafety {
            onBack()
        }

        binding.btnCamera.clickSafety {
            clickCamera()
        }
    }

    private fun clickCamera() {
        if (hasCameraPermission()) {
            takePicture()
        } else {
            requestCameraPermission()
        }
    }

    //region take picture
    private fun takePicture() {
        Constants.isRequestPermission = true
        val photoFile = createImageFile()
        uriPhoto = getUriByFileProvider(photoFile)
        takePictureLauncher.launch(uriPhoto!!)
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        delayResetFlagPermission()
        if (isSuccess) {
            uriPhoto?.let { uri ->
                Log.d(TAG, "Selected URI: $uri")
                uriPhoto = uri
                orientationUri = getOrientationImage(uri)
                initUriBitmap()
            } ?: kotlin.run {
                uriPhoto = null
                orientationUri = null
            }
        }
    }
    //endregion

    private fun initUriBitmap() {
        if (uriPhoto == null) {
            return
        }
        val uri = uriPhoto!!

        if (!orientationUri.isNeedRotateOrFlipImage()) {
            binding.imgCamera.loadSrcNoCache(uri)
            return
        }

        try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.let {
                originalBitmap = BitmapFactory.decodeStream(inputStream)

                originalBitmap?.let { bitmap ->
                    originalBitmap = bitmap.modifyOrientation(orientationUri)
                    originalBitmap?.let {
                        binding.imgCamera.loadSrcNoCache(it)
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    //region request camera permission
    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(CAMERA_PERMISSION)
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                takePicture()
            } else {
                showDialogNeedPermission()
            }
        }

    private fun showDialogNeedPermission() {
        dialogNeedPermission.show("Camera permission is required to take a picture.") {
            gotoDetailSettingsPermission()
        }
    }

    private fun gotoDetailSettingsPermission() {
        Constants.isRequestPermission = true
        try {
            val intentSetting = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", packageName, null)
            }
            requestOpenSettingLauncher.launch(intentSetting)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val requestOpenSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            delayResetFlagPermission()
            if (!hasCameraPermission()) {
                showDialogNeedPermission()
            }
        }
    //endregion
}