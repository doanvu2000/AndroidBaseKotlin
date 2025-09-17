package com.jin.widget.viewgroup

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.viewbinding.ViewBinding

/**
 * An abstract base class for creating custom `ViewGroup`s that utilize `ViewBinding`.
 * This class simplifies the setup of a custom view by handling `ViewBinding` inflation,
 * providing a structured initialization process, and integrating with the AndroidX `Lifecycle`.
 *
 * It acts as a `LifecycleOwner`, allowing the custom view to have its own lifecycle, which can be
 * attached to the lifecycle of a hosting `Activity` or `Fragment`. This is useful for managing
 * resources and operations that need to be aware of the host's lifecycle state (e.g., pausing
 * animations, releasing resources).
 *
 * Subclasses must implement the `initView()`, `initData()`, and `initListener()` methods
 * to set up the view, load data, and register event listeners, respectively.
 *
 * Example Usage:
 * ```kotlin
 * class MyCustomView @JvmOverloads constructor(
 *     context: Context,
 *     attrs: AttributeSet? = null,
 *     defStyleAttr: Int = 0
 * ) : BaseViewGroupCustomView<MyCustomViewBinding>(
 *     context,
 *     attrs,
 *     defStyleAttr,
 *     MyCustomViewBinding::inflate
 * ) {
 *
 *     override fun initView() {
 *         // Setup views using binding.textView.text = "Hello"
 *     }
 *
 *     override fun initData() {
 *         // Load initial data for the view
 *     }
 *
 *     override fun initListener() {
 *         // Set click listeners, e.g., binding.button.setOnClickListener { ... }
 *     }
 *
 *     override fun onPauseCustomView() {
 *         // Custom logic to execute when the host is paused
 *         super.onPauseCustomView()
 *     }
 *
 *     override fun onDestroyCustomView() {
 *         // Clean up resources when the host is destroyed
 *         super.onDestroyCustomView()
 */
abstract class BaseViewGroupCustomView<VB : ViewBinding>(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    bindingInflater: (LayoutInflater, FrameLayout, Boolean) -> VB,
) : FrameLayout(context, attrs, defStyleAttr), LifecycleOwner {

    companion object {
        const val TAG = "CustomView"
    }

    protected val binding: VB
    private val lifecycleRegistry = LifecycleRegistry(this)
    private var lifecycleObserver: LifecycleEventObserver? = null
    private var isLifecycleAttached: Boolean = false

    init {
        binding = bindingInflater.invoke(LayoutInflater.from(context), this, true)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
//        addView(binding.root)
        initialize()
    }

    private fun initialize() {
        initView()
        initData()
        initListener()
    }

    abstract fun initView()
    abstract fun initData()
    abstract fun initListener()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    /**
     * Attaches the custom view's lifecycle to an external [LifecycleOwner] (e.g., an Activity or Fragment).
     * This allows the custom view to observe the lifecycle events of its host and react accordingly,
     * for example, by calling [onPauseCustomView] and [onDestroyCustomView].
     *
     * The method ensures that it only attaches the lifecycle once by checking the [isLifecycleAttached] flag.
     * Upon attachment, it creates a [LifecycleEventObserver] to listen for `ON_PAUSE` and `ON_DESTROY` events.
     * The custom view's own lifecycle state is then moved to `STARTED`.
     *
     * @param lifecycleOwner The parent [LifecycleOwner] whose lifecycle this view should observe.
     */
    fun attachLifecycle(lifecycleOwner: LifecycleOwner) {
        if (isLifecycleAttached) {
            return
        }

        lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    onPauseCustomView()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    onDestroyCustomView()
                }

                else -> {}
            }
        }

        lifecycleObserver?.let {
            lifecycleOwner.lifecycle.addObserver(it)
        }
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        isLifecycleAttached = true
    }

    /**
     * Implement action when onPause Fragment/Activity
     */
    protected open fun onPauseCustomView() {

    }

    /**
     * Implement action when onDestroy Fragment/Activity
     */
    protected open fun onDestroyCustomView() {
        if (isLifecycleAttached) {
            lifecycleObserver?.let {
                lifecycle.removeObserver(it)
            }
        }

        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        lifecycleObserver = null
        isLifecycleAttached = false
    }

    fun isLifecycleAttached() = isLifecycleAttached
}