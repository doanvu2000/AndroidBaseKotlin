package com.base.cameraview.filter

import com.base.cameraview.filters.AutoFixFilter
import com.base.cameraview.filters.BlackAndWhiteFilter
import com.base.cameraview.filters.BrightnessFilter
import com.base.cameraview.filters.ContrastFilter
import com.base.cameraview.filters.CrossProcessFilter
import com.base.cameraview.filters.DocumentaryFilter
import com.base.cameraview.filters.DuotoneFilter
import com.base.cameraview.filters.FillLightFilter
import com.base.cameraview.filters.GammaFilter
import com.base.cameraview.filters.GrainFilter
import com.base.cameraview.filters.GrayscaleFilter
import com.base.cameraview.filters.HueFilter
import com.base.cameraview.filters.InvertColorsFilter
import com.base.cameraview.filters.LomoishFilter
import com.base.cameraview.filters.PosterizeFilter
import com.base.cameraview.filters.SaturationFilter
import com.base.cameraview.filters.SepiaFilter
import com.base.cameraview.filters.SharpnessFilter
import com.base.cameraview.filters.TemperatureFilter
import com.base.cameraview.filters.TintFilter
import com.base.cameraview.filters.VignetteFilter

/**
 * Contains commonly used [Filter]s.
 *
 *
 * You can use [.newInstance] to create a new instance and
 */
enum class Filters(private val filterClass: Class<out Filter>) {
    /**
     * @see NoFilter
     */
    NONE(NoFilter::class.java),

    /**
     * @see AutoFixFilter
     */
    AUTO_FIX(AutoFixFilter::class.java),

    /**
     * @see BlackAndWhiteFilter
     */
    BLACK_AND_WHITE(BlackAndWhiteFilter::class.java),

    /**
     * @see BrightnessFilter
     */
    BRIGHTNESS(BrightnessFilter::class.java),

    /**
     * @see ContrastFilter
     */
    CONTRAST(ContrastFilter::class.java),

    /**
     * @see CrossProcessFilter
     */
    CROSS_PROCESS(CrossProcessFilter::class.java),

    /**
     * @see DocumentaryFilter
     */
    DOCUMENTARY(DocumentaryFilter::class.java),

    /**
     * @see DuotoneFilter
     */
    DUOTONE(DuotoneFilter::class.java),

    /**
     * @see FillLightFilter
     */
    FILL_LIGHT(FillLightFilter::class.java),

    /**
     * @see GammaFilter
     */
    GAMMA(GammaFilter::class.java),

    /**
     * @see GrainFilter
     */
    GRAIN(GrainFilter::class.java),

    /**
     * @see GrayscaleFilter
     */
    GRAYSCALE(GrayscaleFilter::class.java),

    /**
     * @see HueFilter
     */
    HUE(HueFilter::class.java),

    /**
     * @see InvertColorsFilter
     */
    INVERT_COLORS(InvertColorsFilter::class.java),

    /**
     * @see LomoishFilter
     */
    LOMOISH(LomoishFilter::class.java),

    /**
     * @see PosterizeFilter
     */
    POSTERIZE(PosterizeFilter::class.java),

    /**
     * @see SaturationFilter
     */
    SATURATION(SaturationFilter::class.java),

    /**
     * @see SepiaFilter
     */
    SEPIA(SepiaFilter::class.java),

    /**
     * @see SharpnessFilter
     */
    SHARPNESS(SharpnessFilter::class.java),

    /**
     * @see TemperatureFilter
     */
    TEMPERATURE(TemperatureFilter::class.java),

    /**
     * @see TintFilter
     */
    TINT(TintFilter::class.java),

    /**
     * @see VignetteFilter
     */
    VIGNETTE(VignetteFilter::class.java);

    /**
     * Returns a new instance of the given filter.
     *
     * @return a new instance
     */
    fun newInstance(): Filter {
        return try {
            filterClass.newInstance()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            NoFilter()
        } catch (e: InstantiationException) {
            e.printStackTrace()
            NoFilter()
        }
    }
}
