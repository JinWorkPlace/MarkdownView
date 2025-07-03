package io.noties.markwon.movement

import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.widget.TextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.core.CorePlugin

/**
 * @since 3.0.0
 */
class MovementMethodPlugin internal constructor(
    private val movementMethod: MovementMethod?
) : AbstractMarkwonPlugin() {
    override fun configure(registry: MarkwonPlugin.Registry) {
        registry.require(CorePlugin::class.java).hasExplicitMovementMethod(true)
    }

    override fun beforeSetText(textView: TextView, markdown: Spanned) {
        // @since 4.5.0 check for equality
        val current = textView.movementMethod
        if (current != movementMethod) {
            textView.movementMethod = movementMethod
        }
    }

    companion object {
        /**
         * Creates plugin that will ensure that there is movement method registered on a TextView.
         * Uses Android system LinkMovementMethod as default
         *
         * @see .create
         * @see .link
         */
        @Deprecated("4.5.0 use {@link #link()}")
        fun create(): MovementMethodPlugin {
            return create(LinkMovementMethod.getInstance())
        }

        /**
         * @since 4.5.0
         */
        fun link(): MovementMethodPlugin {
            return create(LinkMovementMethod.getInstance())
        }

        /**
         * Special [MovementMethodPlugin] that is **not** applying a MovementMethod on a TextView
         * implicitly
         *
         * @since 4.5.0
         */
        fun none(): MovementMethodPlugin {
            return MovementMethodPlugin(null)
        }

        fun create(movementMethod: MovementMethod): MovementMethodPlugin {
            return MovementMethodPlugin(movementMethod)
        }
    }
}
