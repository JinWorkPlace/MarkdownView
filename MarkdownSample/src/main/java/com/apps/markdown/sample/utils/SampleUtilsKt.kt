package com.apps.markdown.sample.utils

import android.content.Context
import android.util.Log
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.sample.Sample
import java.io.InputStream

private const val TAG = "SampleUtilsKt"

val MarkwonArtifact.displayName: String
    get() = "@${artifactName()}"

val String.tagDisplayName: String
    get() = "#$this"

private const val SAMPLE_PREFIX = "io.noties.markwon.app."

fun Sample.readCode(context: Context): Sample.Code {
    val assets = context.assets

    // keep sample and nested directories
    val path = javaClassName.removePrefix(SAMPLE_PREFIX).replace('.', '/')

    fun obtain(path: String): InputStream? {
        return try {
            assets.open(path)
        } catch (t: Throwable) {
            Log.e(TAG, "obtain: ${t.message}", t)
            null
        }
    }

    // now, we have 2 possibilities -> Kotlin or Java
    var language: Sample.Language = Sample.Language.KOTLIN
    var stream = obtain("$path.kt")
    if (stream == null) {
        language = Sample.Language.JAVA
        stream = obtain("$path.java")
    }

    if (stream == null) {
        throw IllegalStateException("Cannot obtain sample file at path: $path")
    }

    val code = stream.readStringAndClose()

    return Sample.Code(language, code)
}

fun loadReadMe(context: Context): String {
    val stream = context.assets.open("README.md")
    return stream.readStringAndClose()
}