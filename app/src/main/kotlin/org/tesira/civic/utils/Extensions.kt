package org.tesira.civic.utils


import android.content.Context
import android.content.res.Configuration
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Checks if the device is currently in landscape orientation.
 */
fun Context.isLandscape(): Boolean {
    return this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

/**
 * Checks if the device is currently in portrait orientation.
 */
fun Context.isPortrait(): Boolean {
    return this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}


/**
 * Wendet Systemleisten-Insets (links und rechts für die Navigation Bar, oben für die Statusleiste)
 * als zusätzliches Padding auf diese View an, unter Beibehaltung des ursprünglichen Paddings.
 *
 * Diese Funktion ist besonders nützlich für Edge-to-Edge-Layouts, um sicherzustellen,
 * dass Inhalte nicht von Systemleisten überlagert werden.
 */
fun View.applySystemBarInsetsAsPadding() {
    val initialPaddingLeft = this.paddingLeft
    val initialPaddingTop = this.paddingTop
    val initialPaddingRight = this.paddingRight
    val initialPaddingBottom = this.paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->
        val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

        v.updatePadding(
            left = initialPaddingLeft + systemBarInsets.left,
            top = initialPaddingTop,
            right = initialPaddingRight + systemBarInsets.right,
            bottom = initialPaddingBottom + systemBarInsets.bottom
        )
        windowInsets
    }
}

/**
 * Eine spezifischere Version, die nur horizontale Systemleisten-Insets (links/rechts)
 * als zusätzliches Padding anwendet und das obere/untere Padding unverändert lässt oder
 * nur das untere Inset für die Navigationsleiste (im Gestenmodus) berücksichtigt.
 *
 * Diese Funktion ist oft für Container gedacht, die sich über die volle Breite erstrecken sollen,
 * aber nicht unter die Statusleiste oder die Gesten-Navigationsleiste rutschen sollen.
 */
fun View.applyHorizontalSystemBarInsetsAsPadding(applyBottomInset: Boolean = true) {
    val initialPaddingLeft = this.paddingLeft
    val initialPaddingTop = this.paddingTop
    val initialPaddingRight = this.paddingRight
    val initialPaddingBottom = this.paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->
        val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

        v.updatePadding(
            left = initialPaddingLeft + systemBarInsets.left,
            top = initialPaddingTop + systemBarInsets.top,
            right = initialPaddingRight + systemBarInsets.right,
            bottom = if (applyBottomInset) initialPaddingBottom + systemBarInsets.bottom else initialPaddingBottom
        )
        windowInsets
//        WindowInsetsCompat.CONSUMED
    }
}

fun View.applyDefaultSystemBarInsetsAsPadding() {

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->
        val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

        v.updatePadding(
            top = systemBarInsets.top,
            bottom = systemBarInsets.bottom
        )
//        windowInsets
        WindowInsetsCompat.CONSUMED
    }
}