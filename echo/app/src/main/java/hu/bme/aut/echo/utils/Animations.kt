package hu.bme.aut.echo.utils

import android.view.View

fun View.startAnimationFromBottom(delay: Long, duration: Long = 500, distance: Float = 50f) {
    this.translationY = distance
    this.alpha = 0f
    this.animate()
        .translationY(0f)
        .alpha(1f)
        .setStartDelay(delay)
        .setDuration(duration)
        .start()
}

fun View.fadeIn(delay: Long, duration: Long = 500) {
    this.alpha = 0f
    this.animate()
        .alpha(1f)
        .setStartDelay(delay)
        .setDuration(duration)
        .start()
}