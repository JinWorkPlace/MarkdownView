package com.apps.markdown.sample.utils

interface Cancellable {
    val isCancelled: Boolean

    fun cancel()
}