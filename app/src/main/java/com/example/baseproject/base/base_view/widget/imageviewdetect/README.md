# ImageViewDetect

A custom ImageView Implementation

Supports: Detect when drawable loaded

## Implementation

### Extends the standard `AppCompatImageView` api with a few minor additions

#### Add to layout

```xml

<com.example.baseproject.base.base_view.widget.imageviewdetect.ImageViewDetect
    android:layout_width="100dp" android:layout_height="100dp" />
```

#### Drawable callback

```kotlin
imageView.onDrawableLoaded {
    // invoked when any image is loaded, like from a library like Glide, picasso or coil
    // can be used for updating progress UI
}
```

## Credits

A litter view
from [ZoomImageView](https://github.com/doanvu2000/AndroidBaseKotlin/blob/master/app/src/main/java/com/example/baseproject/base/base_view/widget/zoomimageview/README.md)

## License and usage

Feel free to use this file in your code.