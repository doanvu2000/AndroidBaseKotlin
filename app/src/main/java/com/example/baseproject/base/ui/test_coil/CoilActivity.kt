package com.example.baseproject.base.ui.test_coil

import android.util.Log
import android.view.LayoutInflater
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.transform.RoundedCornersTransformation
import com.example.baseproject.R
import com.example.baseproject.base.base_view.BaseActivity
import com.example.baseproject.base.utils.extension.dp
import com.example.baseproject.databinding.ActivityCoilBinding
import kotlin.properties.Delegates

class CoilActivity : BaseActivity<ActivityCoilBinding>() {

    private val link = "https://m.media-amazon.com/images/I/51b0gR8NbGL._UXNaN_FMjpg_QL85_.jpg"

    private var test by Delegates.vetoable(0) { _, oldValue, newValue ->
        newValue > oldValue
    }

    override fun initView() {
    }

    override fun initData() {
        val dimenRound = 10.dp
        binding.imgSource.load(link) {
            lifecycle(this@CoilActivity)
            crossfade(true)
            crossfade(500)
            placeholder(R.drawable.ic_launcher_foreground)
            error(R.drawable.ic_info)
            transformations(RoundedCornersTransformation(dimenRound.toFloat()))
            listener(object : ImageRequest.Listener {
                override fun onStart(request: ImageRequest) {
                    super.onStart(request)
                    Log.d(TAG, "onStart: ")
                }

                override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                    super.onSuccess(request, result)
                    Log.i(TAG, "onSuccess: ")
                }

                override fun onCancel(request: ImageRequest) {
                    super.onCancel(request)
                    Log.w(TAG, "onCancel: ")
                }

                override fun onError(request: ImageRequest, result: ErrorResult) {
                    super.onError(request, result)
                    Log.e(TAG, "onError: ")
                }
            })
        }
    }

    override fun initListener() {

    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityCoilBinding {
        return ActivityCoilBinding.inflate(inflater)
    }
}