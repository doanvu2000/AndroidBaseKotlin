# Skeleton

> The easiest module to show skeleton screens in an Android app.

An Android module that provides an easy way to show skeleton of any view.

Made with ❤ by [Doan Vũ](https://github.com/doanvu2000) base on Koleton lib

## Installation

Import module to your Android project and follow **Quick Start**

## Quick Start

To load the skeleton of a `View`, use the `loadSkeleton` extension function:

```kotlin
// ConstraintLayout
constraintLayout.loadSkeleton()

// TextView
textView?.loadSkeleton(length = 20)

// RecyclerView
recyclerView.loadSkeleton(R.layout.item_example)
```

Skeletons can be configured with an optional trailing lambda:

```kotlin
// ConstraintLayout
constraintLayout.loadSkeleton {
    color(R.color.colorSkeleton)
    cornerRadius(radiusInPixel)
    shimmer(false)
    lineSpacing(spacingInPixel)
}

// TextView
textView?.loadSkeleton(length = 20) {
    color(R.color.colorSkeleton)
    ...
}

// RecyclerView
recyclerView.loadSkeleton(R.layout.item_example) {
    itemCount(3)
    ...
}
```

To hide the skeleton, use the `hideSkeleton` extension function:

```kotlin
view.hideSkeleton()
```

### SkeletonLoader

Skeleton will lazily create a `SkeletonLoader` with the default values.
If you want to set the default values, you can set a default `SkeletonLoader` instance by either:

```kotlin
// In your Application
//class MyApplication : Application(), SkeletonLoaderFactory {
//
//    override fun newSkeletonLoader(): SkeletonLoader {
//        return SkeletonLoader.Builder(this)
//        ...
//        .color(R.color.colorSkeleton)
//            .cornerRadius(radiusInPixel)
//            .build()
//    }
//}
```

Or calling `Koleton.setSkeletonLoader`

```kotlin
val skeletonLoader = SkeletonLoader.Builder(context)
...
.color(R.color.colorSkeleton)
    .cornerRadius(radiusInPixel)
    .build()
Koleton.setSkeletonLoader(skeletonLoader)
```

The default `SkeletonLoader` can be retrieved like so:

```kotlin
val skeletonLoader = Koleton.skeletonLoader(context)
```

### Paging

Koleton works well with `Paging` library with the help of `KoletonView`.

If you want to show a skeleton when you scroll to the bottom of your list, use
the `generateSkeleton` extension function:

```kotlin
// In your PagedListAdapter
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sample, parent, false)
    return when (viewType) {
            ...
            TYPE_SKELETON
        -> SkeletonViewHolder(view.generateSkeleton())
            ...
    }
}

...

class SkeletonViewHolder(val koletonView: KoletonView) : RecyclerView.ViewHolder(koletonView) {
    fun showSkeleton() {
        koletonView.showSkeleton()
    }
}
```

### Shimmer effect

Koleton works with Facebook’s [shimmer](https://github.com/facebook/shimmer-android) library. If you
want to create a custom shimmer effect, you need to include in your dependencies:

```gradle
// In your module's `build.gradle.kts`
dependencies {
    implementation("com.facebook.shimmer:shimmer:0.5.0")
}
```

And set the custom shimmer in the lambda expression:

```kotlin
constraintLayout.loadSkeleton {
    val customShimmer = Shimmer.AlphaHighlightBuilder()
        .setDirection(Shimmer.Direction.TOP_TO_BOTTOM)
    ...
    .build()
    shimmer(customShimmer)
}
```

You can find more information about Facebook's shimmer effect on
the [shimmer-android](http://facebook.github.io/shimmer-android) page.