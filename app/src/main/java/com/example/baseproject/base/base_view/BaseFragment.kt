package com.example.baseproject.base.base_view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.example.baseproject.base.utils.extension.handleBackPressed
import com.example.baseproject.base.utils.util.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    companion object {
        const val TAG = Constant.TAG
    }
    private var _binding: VB? = null
    val binding: VB get() = _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = inflateLayout(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        initListener()
//        handleOnBackPressed()
    }

    private fun handleOnBackPressed() {
        requireActivity().handleBackPressed {
            onBack()
        }
    }

    open fun onBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    abstract fun initView()
    abstract fun initData()
    abstract fun initListener()

    /**override it and inflate your view binding, demo in HomeFragment*/
    abstract fun inflateLayout(inflater: LayoutInflater, container: ViewGroup?): VB

    fun launchAction(dispatcher: CoroutineContext, action: () -> Unit) {
        view?.let {
            viewLifecycleOwner.lifecycleScope.launch(dispatcher) {
                action()
            }
        }
    }

    private fun launchCoroutine(
        dispatcher: CoroutineContext, blockCoroutine: suspend CoroutineScope.() -> Unit
    ) {
        try {
            view?.let {
                viewLifecycleOwner.lifecycleScope.launch(dispatcher) {
                    blockCoroutine()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun launchCoroutineMain(blockCoroutine: suspend CoroutineScope.() -> Unit) {
        launchCoroutine(Dispatchers.Main) {
            blockCoroutine()
        }
    }

    fun launchCoroutineIO(blockCoroutine: suspend CoroutineScope.() -> Unit) {
        launchCoroutine(Dispatchers.IO) {
            blockCoroutine()
        }
    }

    fun launchCoroutineDefault(blockCoroutine: suspend CoroutineScope.() -> Unit) {
        launchCoroutine(Dispatchers.Default) {
            blockCoroutine()
        }
    }
}