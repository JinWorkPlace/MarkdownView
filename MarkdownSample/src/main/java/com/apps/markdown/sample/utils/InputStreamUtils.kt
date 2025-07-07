package com.apps.markdown.sample.utils

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.util.Scanner

private const val TAG = "InputStreamUtils"

fun InputStream.readStringAndClose(): String {
    try {
        val scanner = Scanner(this).useDelimiter("\\A")
        if (scanner.hasNext()) {
            return scanner.next()
        }
        return ""
    } finally {
        try {
            close()
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
        }
    }
}