package com.example.baseproject.base.ui.ads

import com.google.android.gms.ads.nativead.NativeAd

enum class NativeLoadState {
    Loading, Loaded, LoadFailed
}

object NativeCacheManager {
    var nativeCacheMap = mutableMapOf<Int, NativeAd?>()

    var nativeStateMap = mutableMapOf<Int, NativeLoadState>()

    fun clearCache() {
        nativeCacheMap.clear()
        nativeStateMap.clear()
    }

    fun nativeIsReadyInPosition(position: Int): Boolean {
        return nativeStateMap[position] == NativeLoadState.Loaded
    }

    fun nativeIsLoadingInPosition(position: Int): Boolean {
        return nativeStateMap[position] == NativeLoadState.Loading
    }

    fun nativeIsFailedInPosition(position: Int): Boolean {
        return nativeStateMap[position] == NativeLoadState.LoadFailed
    }

    fun nativeInPosition(position: Int): NativeAd? {
        return nativeCacheMap[position]
    }

    fun setNativeFailedInPosition(position: Int) {
        nativeCacheMap[position] = null
        nativeStateMap[position] = NativeLoadState.LoadFailed
    }

    fun setNativeLoadingInPosition(position: Int) {
        nativeStateMap[position] = NativeLoadState.Loading
    }

    fun setNativeLoadedInPosition(position: Int, ad: NativeAd) {
        nativeCacheMap[position] = ad
        nativeStateMap[position] = NativeLoadState.Loaded
    }
}