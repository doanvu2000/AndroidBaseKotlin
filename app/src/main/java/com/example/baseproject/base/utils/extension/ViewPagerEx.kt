package com.example.baseproject.base.utils.extension

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2

//region ViewPager2 Fragment Management

/**
 * Find the current fragment in ViewPager2
 * @param fragmentManager FragmentManager instance
 * @return Current fragment or null if not found
 */
fun ViewPager2.findCurrentFragment(fragmentManager: FragmentManager): Fragment? {
    return fragmentManager.findFragmentByTag("f$currentItem")
}

/**
 * Find fragment at specific position in ViewPager2
 * @param fragmentManager FragmentManager instance
 * @param position position of fragment to find
 * @return Fragment at position or null if not found
 */
fun ViewPager2.findFragmentAtPosition(
    fragmentManager: FragmentManager,
    position: Int
): Fragment? {
    return fragmentManager.findFragmentByTag("f$position")
}

//endregion
