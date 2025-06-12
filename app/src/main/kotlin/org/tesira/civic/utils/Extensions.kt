package org.tesira.civic.utils


import android.content.Context
import android.content.res.Configuration

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