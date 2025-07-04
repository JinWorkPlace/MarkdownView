package io.noties.markwon.image.data

class DataUri(
    private val contentType: String?, private val base64: Boolean, private val data: String?
) {
    fun contentType(): String? {
        return contentType
    }

    fun base64(): Boolean {
        return base64
    }

    fun data(): String? {
        return data
    }

    override fun toString(): String {
        return "DataUri{contentType='$contentType', base64=$base64, data='$data'}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val dataUri = other as DataUri

        if (base64 != dataUri.base64) return false
        if (if (contentType != null) (contentType != dataUri.contentType) else dataUri.contentType != null) return false
        return if (data != null) (data == dataUri.data) else dataUri.data == null
    }

    override fun hashCode(): Int {
        var result = contentType?.hashCode() ?: 0
        result = 31 * result + (if (base64) 1 else 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }
}
