package com.example.baseproject.base.utils.extension

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.baseproject.base.utils.util.AppLogger
import com.example.baseproject.base.utils.util.Constants

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

fun Fragment.safeNavigate(action: Int, bundle: Bundle? = null, navOptions: NavOptions? = null) {
    try {
        findNavController().safeNavigate(action, bundle, navOptions)
    } catch (e: Exception) {
        AppLogger.e(
            Constants.TAG, "${this.javaClass.simpleName} - error:  ${e.stackTraceToString()}"
        )
    }
}

fun Fragment.navigateUp() {
    try {
        findNavController().navigateUp()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}