package com.example.baseproject.base.entity

data class DeviceInfo(
    var model: String = "",
    var manufacturer: String = "",
    var brand: String = "",
    var deviceString: String = "",
    var product: String = "",
    var fingerPrint: String = "",
    var display: String = "",
    var hardware: String = "",
    var host: String = "",
    var id: String = "",
    var tag: String = "",
    var time: String = "",
    var type: String = "",
    var user: String = ""
)

fun deviceInfo(block: DeviceInfo.() -> Unit): DeviceInfo = DeviceInfo().apply(block)