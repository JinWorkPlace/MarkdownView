package io.noties.markwon.image

/**
 * @since 1.0.1
 */
class ImageSize(@JvmField val width: Dimension, @JvmField val height: Dimension) {
    data class Dimension(@JvmField val value: Float, @JvmField val unit: String?) {
        override fun toString(): String {
            return "Dimension{value=$value, unit='$unit'}"
        }
    }

    override fun toString(): String {
        return "ImageSize{width=$width, height=$height}"
    }
}
