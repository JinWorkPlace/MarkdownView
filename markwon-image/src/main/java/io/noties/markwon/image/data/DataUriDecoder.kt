package io.noties.markwon.image.data

import android.text.TextUtils
import android.util.Base64

abstract class DataUriDecoder {
    @Throws(Throwable::class)
    abstract fun decode(dataUri: DataUri): ByteArray?

    internal class Impl : DataUriDecoder() {
        @Throws(Throwable::class)
        override fun decode(dataUri: DataUri): ByteArray? {
            val data = dataUri.data

            return if (!TextUtils.isEmpty(data)) {
                if (dataUri.base64) {
                    Base64.decode(data!!.toByteArray(charset(CHARSET)), Base64.DEFAULT)
                } else {
                    data!!.toByteArray(charset(CHARSET))
                }
            } else {
                null
            }
        }

        companion object {
            private const val CHARSET = "UTF-8"
        }
    }

    companion object {
        @JvmStatic
        fun create(): DataUriDecoder {
            return Impl()
        }
    }
}
