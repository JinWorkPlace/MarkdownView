package com.apps.markdown.sample.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.noties.markwon.app.sample.Sample
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

object SampleUtils {
    fun readSamples(context: Context): MutableList<Sample> {
        try {
            context.assets.open("samples.json").use { inputStream ->
                return readSamples(inputStream)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    // NB! stream is not closed by this method
    fun readSamples(inputStream: InputStream): MutableList<Sample> {
        val gson = Gson()
        return gson.fromJson(
            InputStreamReader(inputStream), object : TypeToken<MutableList<Sample>>() {}.type
        )
    }
}
