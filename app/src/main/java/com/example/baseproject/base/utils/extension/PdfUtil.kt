package com.example.baseproject.base.utils.extension

import android.widget.ScrollView
import androidx.lifecycle.viewModelScope
import com.example.baseproject.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//region PDF Export Utilities

/**
 * Export PDF from ScrollView
 * @param viewModel BaseViewModel instance to handle PDF export
 * @param view ScrollView to export as PDF
 * @param onDone callback when PDF export is completed
 */
private fun exportPdf(
    viewModel: BaseViewModel,
    view: ScrollView,
    onDone: (Any?) -> Unit = {}
) {
    viewModel.exportPdf(
        view,
        view.getChildAt(0).width,
        view.getChildAt(0).height
    ) { pdfFile ->
        // Save file to storage or handle result
        onDone(pdfFile)
    }
}

/**
 * Example function showing concurrent operations with PDF export
 * @param viewModel BaseViewModel instance
 * @param view ScrollView to export
 */
private fun exportPdfWithConcurrentOperations(viewModel: BaseViewModel, view: ScrollView) {
    viewModel.viewModelScope.launch {
        val job1 = async {
            withContext(Dispatchers.IO) {
                // TODO: Implement function1 - background operation 1
            }
        }

        val job2 = async {
            withContext(Dispatchers.IO) {
                // TODO: Implement function2 - background operation 2
            }
        }

        // Wait for both operations to complete
        job1.await()
        job2.await()

        // TODO: Implement final operations after both jobs complete
    }
}

//endregion
