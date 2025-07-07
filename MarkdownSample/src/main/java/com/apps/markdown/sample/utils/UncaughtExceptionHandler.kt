package com.apps.markdown.sample.utils

class UncaughtExceptionHandler(
    private val origin: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread?, e: Throwable?) {

    }
}