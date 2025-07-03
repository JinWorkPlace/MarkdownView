package io.noties.markwon.image.data

@JvmRecord
data class DataUri(@JvmField val contentType: String?, @JvmField val base64: Boolean, @JvmField val data: String?) {
    fun contentType(): String? {
        return contentType
    }

    fun data(): String? {
        return data
    }

    override fun toString(): String {
        return "DataUri{contentType='$contentType', base64=$base64, data='$data'}"
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val dataUri = o as DataUri

        if (base64 != dataUri.base64) return false
        if (if (contentType != null) (contentType != dataUri.contentType) else dataUri.contentType != null) return false
        return if (data != null) (data == dataUri.data) else dataUri.data == null
    }

    override fun hashCode(): Int {
        var result = base64.hashCode()
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }
}
