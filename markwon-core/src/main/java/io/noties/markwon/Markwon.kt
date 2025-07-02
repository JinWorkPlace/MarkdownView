package io.noties.markwon

import android.content.Context
import android.text.Spanned
import android.widget.TextView
import android.widget.TextView.BufferType
import io.noties.markwon.core.CorePlugin
import org.commonmark.node.Node

/**
 * Class to parse and render markdown. Since version 3.0.0 instance specific (previously consisted
 * of static stateless methods). An instance of builder can be obtained via [.builder]
 * method.
 *
 * @see .create
 * @see .builder
 * @see Builder
 */
abstract class Markwon {
    /**
     * Method to parse markdown (without rendering)
     *
     * @param input markdown input to parse
     * @return parsed via commonmark-java `org.commonmark.node.Node`
     * @see .render
     * @since 3.0.0
     */
    abstract fun parse(input: String): Node

    /**
     * Create Spanned markdown from parsed Node (via [.parse] call).
     *
     *
     * Please note that returned Spanned has few limitations. For example, images, tables
     * and ordered lists require TextView to be properly displayed. This is why images and tables
     * most likely won\'t work in this case. Ordered lists might have mis-measurements. Whenever
     * possible use [.setMarkdown] or [.setParsedMarkdown]
     * as these methods will additionally call specific [MarkwonPlugin] methods to *prepare*
     * proper display.
     *
     * @since 3.0.0
     */
    abstract fun render(node: Node): Spanned

    /**
     * This method will [.parse] and [.render] supplied markdown. Returned
     * Spanned has the same limitations as from [.render] method.
     *
     * @param input markdown input
     * @see .parse
     * @see .render
     * @since 3.0.0
     */
    abstract fun toMarkdown(input: String): Spanned

    abstract fun setMarkdown(textView: TextView, markdown: String)

    abstract fun setParsedMarkdown(textView: TextView, markdown: Spanned)

    /**
     * Requests information if certain plugin has been registered. Please note that this
     * method will check for super classes also, so if supplied with `markwon.hasPlugin(MarkwonPlugin.class)`
     * this method (if has at least one plugin) will return true. If for example a custom
     * (subclassed) version of a [CorePlugin] has been registered and given name
     * `CorePlugin2`, then both `markwon.hasPlugin(CorePlugin2.class)` and
     * `markwon.hasPlugin(CorePlugin.class)` will return true.
     *
     * @param plugin type to query
     * @return true if a plugin is used when configuring this [Markwon] instance
     */
    abstract fun hasPlugin(plugin: Class<out MarkwonPlugin>): Boolean

    abstract fun <P : MarkwonPlugin> getPlugin(type: Class<P>): P?

    /**
     * @since 4.1.0
     */
    abstract fun <P : MarkwonPlugin> requirePlugin(type: Class<P>): P

    /**
     * @return a list of registered [MarkwonPlugin]
     * @since 4.1.0
     */
    abstract val plugins: MutableList<out MarkwonPlugin?>

    abstract fun configuration(): MarkwonConfiguration

    /**
     * Interface to set text on a TextView. Primary goal is to give a way to use PrecomputedText
     * functionality
     *
     * @see PrecomputedTextSetterCompat
     *
     * @since 4.1.0
     */
    interface TextSetter {
        /**
         * @param textView   TextView
         * @param markdown   prepared markdown
         * @param bufferType BufferType specified when building [Markwon] instance
         * via [Builder.bufferType]
         * @param onComplete action to run when set-text is finished (required to call in order
         * to execute [MarkwonPlugin.afterSetText])
         */
        fun setText(
            textView: TextView, markdown: Spanned, bufferType: BufferType, onComplete: Runnable
        )
    }

    /**
     * Builder for [Markwon].
     *
     *
     * Please note that the order in which plugins are supplied is important as this order will be
     * used through the whole usage of built Markwon instance
     *
     * @since 3.0.0
     */
    interface Builder {
        /**
         * Specify bufferType when applying text to a TextView `textView.setText(CharSequence,BufferType)`.
         * By default `BufferType.SPANNABLE` is used
         *
         * @param bufferType BufferType
         */
        fun bufferType(bufferType: BufferType): Builder

        /**
         * @param textSetter [TextSetter] to apply text to a TextView
         * @since 4.1.0
         */
        fun textSetter(textSetter: TextSetter): Builder

        fun usePlugin(plugin: MarkwonPlugin): Builder

        fun usePlugins(plugins: Iterable<MarkwonPlugin?>): Builder

        /**
         * Control if small chunks of non-finished markdown sentences (for example, a single `*` character)
         * should be displayed/rendered as raw input instead of an empty string.
         *
         *
         * Since 4.4.0 `true` by default, versions prior - `false`
         *
         * @since 4.4.0
         */
        fun fallbackToRawInputWhenEmpty(fallbackToRawInputWhenEmpty: Boolean): Builder

        fun build(): Markwon
    }

    companion object {
        /**
         * Factory method to create a *minimally* functional [Markwon] instance. This
         * instance will have **only** [CorePlugin] registered. If you wish
         * to configure this instance more consider using [.builder] method.
         *
         * @return [Markwon] instance with only CorePlugin registered
         * @since 3.0.0
         */
        fun create(context: Context): Markwon {
            return builder(context).usePlugin(CorePlugin.create()).build()
        }

        /**
         * Factory method to obtain an instance of [Builder] with [CorePlugin] added.
         *
         * @see Builder
         *
         * @see .builderNoCore
         * @since 3.0.0
         */
        @JvmStatic
        fun builder(context: Context): Builder {
            return MarkwonBuilderImpl(context) // @since 4.0.0 add CorePlugin
                .usePlugin(CorePlugin.create())
        }

        /**
         * Factory method to obtain an instance of [Builder] without [CorePlugin].
         *
         * @since 4.0.0
         */
        fun builderNoCore(context: Context): Builder {
            return MarkwonBuilderImpl(context)
        }
    }
}
