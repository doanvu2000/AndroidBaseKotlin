package com.example.baseproject.base.utils.extension

import android.widget.ScrollView
import androidx.lifecycle.viewModelScope
import com.example.baseproject.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun exportPdf(viewModel: BaseViewModel, view: ScrollView) {
    viewModel.exportPdf(
        view, view.getChildAt(0).width, view.getChildAt(0).height
    ) { pdfFile ->
        //save file to storage
        pdfFile
    }
    viewModel.viewModelScope.launch {
        val job1 = async {
            withContext(Dispatchers.IO) {
                //TODO code function1
            }
        }
        val job2 = async {
            withContext(Dispatchers.IO) {
                //TODO code function2
            }
        }
        job1.await()
        job2.await()
        //TODO code2
    }
}