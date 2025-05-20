package com.example.baseproject.base.ui.network_demo

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.base.base_view.screen.BaseActivity
import com.example.baseproject.base.network.NetWorkViewModel
import com.example.baseproject.base.network.NetworkResult
import com.example.baseproject.base.network.formatFileSize
import com.example.baseproject.base.utils.extension.hide
import com.example.baseproject.base.utils.extension.setTextHtml
import com.example.baseproject.base.utils.extension.show
import com.example.baseproject.databinding.ActivityNetWorkDemoBinding
import kotlinx.coroutines.launch

class NetWorkDemoActivity : BaseActivity<ActivityNetWorkDemoBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityNetWorkDemoBinding {
        return ActivityNetWorkDemoBinding.inflate(inflater)
    }

    private val viewModel by viewModels<NetWorkViewModel>()

    override fun initView() {

    }

    override fun initData() {
        viewModel.initialize(this)
    }

    override fun initListener() {
        binding.fetchButton.clickSafe {
            fetchData()
        }

        binding.cancelButton.clickSafe {
            viewModel.cancelFetch()
            resetUi()
        }

        binding.clearCacheButton.clickSafe {
            viewModel.clearCache()
            binding.resultTextView.text = "Cache cleared"
        }
    }

    private fun resetUi() {
        binding.fetchButton.isEnabled = true
        binding.cancelButton.isEnabled = false
        binding.progressBar.progress = 0
        binding.tvProgress.text = "0%"
        binding.resultTextView.text = "Cancelled"
    }

    private fun showLoading() {
        binding.loadingView.show()
    }

    private fun hideLoading() {
        binding.loadingView.hide()
    }

    @SuppressLint("SetTextI18n")
    private fun fetchData() {
        // Show loading state
        binding.resultTextView.text = "Loading..."
        binding.fetchButton.isEnabled = false
        binding.cancelButton.isEnabled = true
        binding.progressBar.progress = 0

        lifecycleScope.launch {
            showLoading()
            try {
                val url =
                    "https://translate.google.com/?hl=vi&sl=vi&tl=en&text=1000%20k%C3%AD%20t%E1%BB%B1%20%C4%91%E1%BA%A7u%20ti%C3%AAn&op=translate"
                viewModel.fetchString(url = url).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            launchCoroutineMain {
                                hideLoading()
                                val data = result.data.take(1000)
                                binding.resultTextView.setTextHtml("Success(First 1000 chars): $data")
                                binding.cancelButton.isEnabled = false
                                binding.fetchButton.isEnabled = true
                            }
                        }

                        is NetworkResult.Error -> {
                            hideLoading()
                            val errorMessage = when (result.exception) {
                                is java.net.ConnectException -> "Cannot connect to server"
                                is java.io.FileNotFoundException -> "API endpoint not found"
                                is java.net.SocketException -> "Network connection lost"
                                is java.io.EOFException -> "Connection ended unexpectedly"
                                is javax.net.ssl.SSLException -> "SSL/TLS connection failed"
                                is java.security.cert.CertPathValidatorException ->
                                    "Server certificate validation failed"

                                else -> "Error: ${result.exception.message}"
                            }
                            launchCoroutineMain {
                                binding.resultTextView.text = errorMessage
                                binding.cancelButton.isEnabled = false
                                binding.fetchButton.isEnabled = true
                            }
                        }

                        is NetworkResult.Progress -> {
                            launchCoroutineMain {
                                if (result.contentLength > 0) {
                                    Log.d(TAG, "fetchData: ${result.progress} %")
                                    binding.progressBar.progress = result.progress
                                    val downloaded = formatFileSize(result.bytesRead)
                                    val total = formatFileSize(result.contentLength)
                                    binding.tvProgress.text =
                                        "$downloaded / $total (${result.progress}%)"
                                } else {
                                    val downloaded = formatFileSize(result.bytesRead)
                                    binding.tvProgress.text =
                                        "Downloaded: $downloaded (size unknown)"
                                }
                            }
                        }

                    }
                }
            } catch (e: Exception) {
                launchCoroutineMain {
                    hideLoading()
                    binding.resultTextView.text = "Unexpected error: ${e.message}"
                    binding.cancelButton.isEnabled = false
                    binding.fetchButton.isEnabled = true
                }
            }
        }
    }
}