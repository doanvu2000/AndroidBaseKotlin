package com.example.baseproject.base.ui.pick_image

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.Companion.isPhotoPickerAvailable
import com.example.baseproject.base.base_view.BaseActivity
import com.example.baseproject.base.dialog.DialogNeedPermission
import com.example.baseproject.base.utils.extension.READ_EXTERNAL_STORAGE
import com.example.baseproject.base.utils.extension.getOrientationImage
import com.example.baseproject.base.utils.extension.hasReadPermissionBelowQ
import com.example.baseproject.base.utils.extension.isNeedRotateOrFlipImage
import com.example.baseproject.base.utils.extension.isSdkR
import com.example.baseproject.base.utils.extension.loadSrcNoCache
import com.example.baseproject.base.utils.extension.modifyOrientation
import com.example.baseproject.base.utils.util.Constants
import com.example.baseproject.base.utils.util.delayResetFlagPermission
import com.example.baseproject.databinding.ActivityPickImageBinding
import java.io.FileNotFoundException
import java.io.InputStream

class PickImageActivity : BaseActivity<ActivityPickImageBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityPickImageBinding {
        return ActivityPickImageBinding.inflate(inflater)
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
        binding.btnPick.clickSafety {
            clickPickImage()
        }

        binding.btnBack.clickSafety {
            onBack()
        }
    }

    private fun clickPickImage() {
        if (isSdkR()) {
            pickVisualImage()
        } else {
            if (hasReadPermissionBelowQ()) {
                pickImageQAndBelow()
            } else {
                requestReadPermissionQAndBelow()
            }
        }
    }

    //region pick visual image
    private fun pickVisualImage() {
        val isPhotoPickerAvailable = isPhotoPickerAvailable(this)
        Log.d(TAG, "isPhotoPickerAvailable: $isPhotoPickerAvailable")
        Constants.isRequestPermission = true
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the photo picker
            delayResetFlagPermission()
            uri?.let {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, flag)
                uriPhoto = uri
                orientationUri = getOrientationImage(uri)
                initUriBitmap()
            } ?: kotlin.run {
                Log.w(TAG, "No media selected")
                uriPhoto = null
                orientationUri = null
            }
        }
    //endregion

    //region pick image API Q(29) and below
    private fun pickImageQAndBelow() {
        Constants.isRequestPermission = true
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        val chooserIntent = Intent.createChooser(pickIntent, "Select image")
        pickImageBelowQLauncher.launch(chooserIntent)
    }

    private val pickImageBelowQLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                delayResetFlagPermission()
                result.data?.data?.let { uri ->
                    Log.d(TAG, "Selected URI: $uri")
                    uriPhoto = uri
                    orientationUri = getOrientationImage(uri)
                    initUriBitmap()
                } ?: kotlin.run {
                    uriPhoto = null
                    orientationUri = null
                }
            } catch (e: Exception) {
                uriPhoto = null
                orientationUri = null
                e.printStackTrace()
            }
        }
    //endregion

    //region request read permission
    private fun requestReadPermissionQAndBelow() {
        requestReadPermission.launch(READ_EXTERNAL_STORAGE)
    }

    private val requestReadPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGrant ->
            if (isGrant) {
                pickImageQAndBelow()
            } else {
                //show dialog require
                showDialogRequiredPermission()
            }
        }

    private fun showDialogRequiredPermission() {
        dialogNeedPermission.show(onClickSetting = {
            gotoDetailSettingsReadPermission()
        })
    }

    private fun gotoDetailSettingsReadPermission() {
        Constants.isRequestPermission = true
        try {
            val intentSetting = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", packageName, null)
            }
            requestOpenSettingReadPermissionLauncher.launch(intentSetting)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val requestOpenSettingReadPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            delayResetFlagPermission()
            if (!hasReadPermissionBelowQ()) {
                showDialogRequiredPermission()
            }
        }
    //endregion

    private fun initUriBitmap() {
        if (uriPhoto == null) {
            return
        }
        val uri = uriPhoto!!

        if (!orientationUri.isNeedRotateOrFlipImage()) {
            binding.imgPick.loadSrcNoCache(uri)
            return
        }

        var inputStream: InputStream?
        try {
            inputStream = contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            inputStream = null
        }

        if (inputStream != null) {
            originalBitmap = BitmapFactory.decodeStream(inputStream)

            originalBitmap?.let { bitmap ->
                originalBitmap = bitmap.modifyOrientation(orientationUri)
                originalBitmap?.let {
                    binding.imgPick.loadSrcNoCache(it)
                }
            }
        }
    }
}