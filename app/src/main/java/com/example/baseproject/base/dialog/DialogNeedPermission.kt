package com.example.baseproject.base.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.example.baseproject.R
import com.example.baseproject.databinding.DialogRequirePermissionBinding

class DialogNeedPermission(private val context: Context) {
    private val binding by lazy {
        DialogRequirePermissionBinding.inflate(LayoutInflater.from(context))
    }

    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context, R.style.dialog_transparent_width).setView(binding.root)
            .create()
    }

    fun isShowing() = dialog.isShowing

    fun hide() = dialog.dismiss()

    fun show(
        msg: String? = null,
        onClickSetting: (() -> Unit)? = null,
        onClickLater: (() -> Unit)? = null
    ) {
        dialog.setCancelable(true)
        msg?.let { binding.tvMsg.text = it }

        binding.btnSetting.setOnClickListener {
            hide()
            onClickSetting?.invoke()
        }

        binding.btnLater.setOnClickListener {
            hide()
            onClickLater?.invoke()
        }

        if (!isShowing()) {
            dialog.show()
        }
    }
}