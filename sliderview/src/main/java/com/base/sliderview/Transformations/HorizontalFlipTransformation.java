package com.base.sliderview.Transformations;

import android.util.Log;
import android.view.View;

import com.base.sliderview.SliderPager;

public class HorizontalFlipTransformation implements SliderPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {

        page.setTranslationX(-position * page.getWidth());
        page.setCameraDistance(20000);

        if (position < 0.5 && position > -0.5) {
            page.setVisibility(View.VISIBLE);
        } else {
            page.setVisibility(View.INVISIBLE);
        }


        if (position < -1) {     // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.setAlpha(0);

        } else if (position <= 0) {    // [-1,0]
            page.setAlpha(1);
            page.setRotationX(180 * (1 - Math.abs(position) + 1));
            Log.e("HORIZONTAL", "position <= 0     " + (180 * (1 - Math.abs(position) + 1)));

        } else if (position <= 1) {    // (0,1]
            page.setAlpha(1);
            page.setRotationX(-180 * (1 - Math.abs(position) + 1));
            Log.e("HORIZONTAL", "position <= 1     " + (-180 * (1 - Math.abs(position) + 1)));

        } else {    // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(0);

        }


    }
}