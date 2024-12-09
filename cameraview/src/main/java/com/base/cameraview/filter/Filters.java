package com.base.cameraview.filter;

import androidx.annotation.NonNull;

import com.base.cameraview.filters.AutoFixFilter;
import com.base.cameraview.filters.BlackAndWhiteFilter;
import com.base.cameraview.filters.BrightnessFilter;
import com.base.cameraview.filters.ContrastFilter;
import com.base.cameraview.filters.CrossProcessFilter;
import com.base.cameraview.filters.DocumentaryFilter;
import com.base.cameraview.filters.DuotoneFilter;
import com.base.cameraview.filters.FillLightFilter;
import com.base.cameraview.filters.GammaFilter;
import com.base.cameraview.filters.GrainFilter;
import com.base.cameraview.filters.GrayscaleFilter;
import com.base.cameraview.filters.HueFilter;
import com.base.cameraview.filters.InvertColorsFilter;
import com.base.cameraview.filters.LomoishFilter;
import com.base.cameraview.filters.PosterizeFilter;
import com.base.cameraview.filters.SaturationFilter;
import com.base.cameraview.filters.SepiaFilter;
import com.base.cameraview.filters.SharpnessFilter;
import com.base.cameraview.filters.TemperatureFilter;
import com.base.cameraview.filters.TintFilter;
import com.base.cameraview.filters.VignetteFilter;

/**
 * Contains commonly used {@link Filter}s.
 * <p>
 * You can use {@link #newInstance()} to create a new instance and
 */
public enum Filters {

    /**
     * @see NoFilter
     */
    NONE(NoFilter.class),

    /**
     * @see AutoFixFilter
     */
    AUTO_FIX(AutoFixFilter.class),

    /**
     * @see BlackAndWhiteFilter
     */
    BLACK_AND_WHITE(BlackAndWhiteFilter.class),

    /**
     * @see BrightnessFilter
     */
    BRIGHTNESS(BrightnessFilter.class),

    /**
     * @see ContrastFilter
     */
    CONTRAST(ContrastFilter.class),

    /**
     * @see CrossProcessFilter
     */
    CROSS_PROCESS(CrossProcessFilter.class),

    /**
     * @see DocumentaryFilter
     */
    DOCUMENTARY(DocumentaryFilter.class),

    /**
     * @see DuotoneFilter
     */
    DUOTONE(DuotoneFilter.class),

    /**
     * @see FillLightFilter
     */
    FILL_LIGHT(FillLightFilter.class),

    /**
     * @see GammaFilter
     */
    GAMMA(GammaFilter.class),

    /**
     * @see GrainFilter
     */
    GRAIN(GrainFilter.class),

    /**
     * @see GrayscaleFilter
     */
    GRAYSCALE(GrayscaleFilter.class),

    /**
     * @see HueFilter
     */
    HUE(HueFilter.class),

    /**
     * @see InvertColorsFilter
     */
    INVERT_COLORS(InvertColorsFilter.class),

    /**
     * @see LomoishFilter
     */
    LOMOISH(LomoishFilter.class),

    /**
     * @see PosterizeFilter
     */
    POSTERIZE(PosterizeFilter.class),

    /**
     * @see SaturationFilter
     */
    SATURATION(SaturationFilter.class),

    /**
     * @see SepiaFilter
     */
    SEPIA(SepiaFilter.class),

    /**
     * @see SharpnessFilter
     */
    SHARPNESS(SharpnessFilter.class),

    /**
     * @see TemperatureFilter
     */
    TEMPERATURE(TemperatureFilter.class),

    /**
     * @see TintFilter
     */
    TINT(TintFilter.class),

    /**
     * @see VignetteFilter
     */
    VIGNETTE(VignetteFilter.class);

    private Class<? extends Filter> filterClass;

    Filters(@NonNull Class<? extends Filter> filterClass) {
        this.filterClass = filterClass;
    }

    /**
     * Returns a new instance of the given filter.
     *
     * @return a new instance
     */
    @NonNull
    public Filter newInstance() {
        try {
            return filterClass.newInstance();
        } catch (IllegalAccessException e) {
            return new NoFilter();
        } catch (InstantiationException e) {
            return new NoFilter();
        }
    }
}
