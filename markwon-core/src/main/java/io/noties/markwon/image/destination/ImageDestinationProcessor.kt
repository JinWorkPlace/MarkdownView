package io.noties.markwon.image.destination

/**
 * Process destination of image nodes
 *
 * @since 4.4.0
 */
abstract class ImageDestinationProcessor {
    abstract fun process(destination: String): String

    private class NoOp : ImageDestinationProcessor() {
        override fun process(destination: String): String {
            return destination
        }
    }

    companion object {
        @JvmStatic
        fun noOp(): ImageDestinationProcessor {
            return NoOp()
        }
    }
}
