package com.example.baseproject.base.ui.camerademo

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.baseproject.R
import com.example.baseproject.databinding.DialogInitCameraBinding

class DialogInitializeCamera(private val context: Context) {
    private val binding by lazy {
        DialogInitCameraBinding.inflate(LayoutInflater.from(context))
    }
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context, R.style.dialog_transparent_width).setView(binding.root)
            .create()
    }

    private fun isShowing() = dialog.isShowing
    fun hide() = dialog.dismiss()
    fun show() {
        dialog.setCancelable(false)
        if (!isShowing()) {
            dialog.show()
        }
    }

}