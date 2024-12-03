package com.example.baseproject.base.ui.location

import android.annotation.SuppressLint
import android.location.Location
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import com.example.baseproject.base.base_view.screen.BaseActivity
import com.example.baseproject.base.utils.extension.ACCESS_COARSE_LOCATION
import com.example.baseproject.base.utils.extension.ACCESS_FINE_LOCATION
import com.example.baseproject.base.utils.extension.finishWithSlide
import com.example.baseproject.base.utils.extension.getLocationUser
import com.example.baseproject.base.utils.extension.gone
import com.example.baseproject.base.utils.extension.isGpsEnable
import com.example.baseproject.base.utils.extension.isInternetAvailable
import com.example.baseproject.base.utils.extension.show
import com.example.baseproject.base.utils.util.LocationUtils
import com.example.baseproject.databinding.ActivityLocationBinding

class LocationActivity : BaseActivity<ActivityLocationBinding>() {
    override fun initView() {

    }

    override fun initData() {

    }

    override fun initListener() {
        binding.btnBack.clickSafety {
            onBack()
        }
        binding.btnGetLocation.clickSafety {
            if (isGpsEnable() && isInternetAvailable()) {
                getLocation()
            } else {
                setStatus("please enable gps and network to get location and address!!")
            }
        }
    }

    override fun onBack() {
        finishWithSlide()
    }

    @SuppressLint("SetTextI18n")
    private fun getLocation() {
        clearText()
        showLoading()
        setStatus("get location user...")
        getLocationUser(onGetLocationComplete = {
            setStatus("get location success 💚🧡💛")
            binding.tvLatitude.text = "Latitude: " + it.latitude.toString()
            binding.tvLongitude.text = "Longitude: " + it.longitude.toString()
            getAddress(it)
        }, onFail = {
            hideLoading()
            setStatus("get location failed 💔💔")
        }, onMissingPermission = {
            hideLoading()
            setStatus("Missing location permission 💢💢")
            permissionLauncher.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        })
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it[ACCESS_FINE_LOCATION] == true && it[ACCESS_COARSE_LOCATION] == true) {
            setStatus("permission ok, please retry to get location")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getAddress(location: Location) {
        setStatus("get address...")
        LocationUtils.getAddressNameByLocation(
            this, location.latitude, location.longitude
        ) { address ->
            runOnUiThread {
                hideLoading()
                address?.let {
                    if (it.isNotEmpty()) {
                        setStatus("Get address success")
                        val pos = it.first()
                        val result = pos.getAddressLine(0)
                        binding.tvAddress.text = "Address: $result"
                    } else {
                        setStatus("address is empty❌")
                    }
                } ?: kotlin.run {
                    setStatus("get address failed 💔")
                }
            }
        }

    }

    private fun showLoading() {
        binding.loadingView.show()
    }

    private fun hideLoading() {
        binding.loadingView.gone()
    }

    @SuppressLint("SetTextI18n")
    private fun setStatus(status: String) {
        binding.tvStatus.text = "Status: $status"
    }

    @SuppressLint("SetTextI18n")
    private fun clearText() {
        binding.tvLatitude.text = "Lat: "
        binding.tvLongitude.text = "Long: "
        binding.tvAddress.text = "Address: "
        setStatus("")
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityLocationBinding {
        return ActivityLocationBinding.inflate(inflater)
    }
}