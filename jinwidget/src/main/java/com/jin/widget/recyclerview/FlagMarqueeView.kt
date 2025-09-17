package com.jin.widget.recyclerview

import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso


/**
 * A custom view that displays a horizontal marquee of images (flags) and randomly selects one.
 *
 * This view uses a [RecyclerView] to create an infinitely scrolling list of images.
 * The [startMarquee] method initiates a smooth scrolling animation for a specified duration,
 * after which it stops and randomly selects one of the displayed images, invoking a callback
 * with the index of the picked image.
 *
 * Usage:
 * 1. Add `FlagMarqueeView` to your layout XML.
 * 2. Get a reference to the view in your Activity/Fragment.
 * 3. Call [setImages] to provide a list of image URLs or resource identifiers.
 * 4. Call [startMarquee] to begin the animation and selection process.
 * 5. Use [stopMarquee] or [cancelMarquee] to control the animation manually if needed.
 *
 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource
 *                     that supplies default values for the view. Can be 0 to not look for defaults.
 */
class FlagMarqueeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val recyclerView: RecyclerView
    private var adapter: FlagAdapter? = null
    private var animator: ValueAnimator? = null
    private var images: List<String> = emptyList()

    // Handler & Runnable để stop và random
    private val handler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null

    init {
        // Khởi tạo RecyclerView
        recyclerView = RecyclerView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
            )
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            overScrollMode = OVER_SCROLL_NEVER
        }
        addView(recyclerView)
    }

    fun setImages(imgs: List<String>) {
        images = imgs
        adapter = FlagAdapter(images)
        recyclerView.adapter = adapter
    }

    fun startMarquee(durationMs: Long = 3500L, onRandomPicked: (index: Int) -> Unit) {
        cancelMarquee()

        animator = ValueAnimator.ofInt(0, 500).apply {
            duration = durationMs
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                recyclerView.scrollBy(20, 0)
            }
            start()
        }

        stopRunnable = Runnable {
            stopMarquee()
            if (images.isNotEmpty()) {
                val randomIndex = images.indices.random()
                onRandomPicked(randomIndex)
            }
        }
        handler.postDelayed(stopRunnable!!, durationMs)
    }

    fun stopMarquee() {
        animator?.cancel()
        animator = null
    }

    fun cancelMarquee() {
        stopMarquee()
        stopRunnable?.let { handler.removeCallbacks(it) }
        stopRunnable = null
    }

    // Adapter nội bộ
    private class FlagAdapter(val imgs: List<String>) :
        RecyclerView.Adapter<FlagAdapter.FlagViewHolder>() {

        inner class FlagViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlagViewHolder {
            val img = ImageView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(200, 200)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            return FlagViewHolder(img)
        }

        override fun onBindViewHolder(holder: FlagViewHolder, position: Int) {
            //todo: update logic load src to imageView
            val src = imgs[position % imgs.size]
            Picasso.get()
                .load(src)
//                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(holder.imageView)
//            holder.imageView.loadSrcCacheAll(imgs[position % imgs.size])
        }

        override fun getItemCount(): Int = Int.MAX_VALUE // chạy vòng lặp
    }
}
