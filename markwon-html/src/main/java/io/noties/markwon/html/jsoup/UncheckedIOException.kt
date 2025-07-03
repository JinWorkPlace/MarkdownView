package io.noties.markwon.html.jsoup

import java.io.IOException

class UncheckedIOException(cause: IOException) : RuntimeException(cause) {
    fun ioException(): IOException {
        return cause as IOException
    }
}
