package com.example.baseproject.base.ui.ads

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.example.baseproject.R
import com.example.baseproject.databinding.LayoutDialogLoadingInterBinding

class DialogLoadingInter(private val context: Context) {
    private val binding by lazy {
        LayoutDialogLoadingInterBinding.inflate(LayoutInflater.from(context))
    }

    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context, R.style.full_screen_dialog).setView(binding.root)
            .create()
    }

    private fun isShowing(): Boolean {
        return dialog.isShowing
    }

    fun hide() {
        if (isShowing()) {
            dialog.dismiss()
        }
    }

    fun show() {
        dialog.setCancelable(false)
        if (!isShowing()) {
            dialog.show()
        }
    }
}