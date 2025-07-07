package com.apps.markdown.sample.utils

import java.io.PrintWriter
import java.io.StringWriter

object ThrowableUtils

fun Throwable.stackTraceString(): String {
    val stringWriter = StringWriter()
    val printWriter = PrintWriter(stringWriter)
    this.printStackTrace(printWriter)
    return stringWriter.toString()
}