package com.example.baseproject.base.utils.extension

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.baseproject.base.utils.util.AppLogger
import com.example.baseproject.base.utils.util.Constants

//region Safe Navigation

/**
 * Safe navigate để tránh crash khi navigate multiple times
 * @param actionId ID của action navigation
 * @param bundle data bundle truyền đi
 * @param navOptions navigation options
 */
fun NavController.safeNavigate(
    actionId: Int,
    bundle: Bundle? = null,
    navOptions: NavOptions? = null,
) {
    val action = currentDestination?.getAction(actionId) ?: graph.getAction(actionId)
    if (action != null && currentDestination?.id != action.destinationId) {
        navigate(actionId, bundle, navOptions)
    }
}

/**
 * Safe navigate từ Fragment với error handling
 * @param action ID của action navigation
 * @param bundle data bundle truyền đi
 * @param navOptions navigation options
 */
fun Fragment.safeNavigate(action: Int, bundle: Bundle? = null, navOptions: NavOptions? = null) {
    try {
        findNavController().safeNavigate(action, bundle, navOptions)
    } catch (e: Exception) {
        AppLogger.e(
            Constants.TAG, "${this.javaClass.simpleName} - error: ${e.stackTraceToString()}"
        )
    }
}

//endregion

//region Navigation Actions

/**
 * Navigate up với error handling
 */
fun Fragment.navigateUp() {
    try {
        findNavController().navigateUp()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

//endregion
