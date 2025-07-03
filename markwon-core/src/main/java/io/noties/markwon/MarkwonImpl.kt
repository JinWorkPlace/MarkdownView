package io.noties.markwon

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.widget.TextView
import android.widget.TextView.BufferType
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import java.util.Locale

/**
 * @since 3.0.0
 */
internal data class MarkwonImpl(
    private val bufferType: BufferType, // @since 4.1.0
    private val textSetter: TextSetter?,
    private val parser: Parser, // @since 4.1.1
    private val visitorFactory: MarkwonVisitorFactory,
    private val configuration: MarkwonConfiguration,
    override val plugins: MutableList<out MarkwonPlugin>,
    private val fallbackToRawInputWhenEmpty: Boolean,
) : Markwon() {
    override fun parse(input: String): Node {
        // make sure that all plugins are called `processMarkdown` before parsing

        var input = input
        for (plugin in plugins) {
            input = plugin.processMarkdown(input)
        }

        return parser.parse(input)
    }

    override fun render(node: Node): Spanned {
        for (plugin in plugins) {
            plugin.beforeRender(node)
        }

        // @since 4.1.1 obtain visitor via factory
        val visitor = visitorFactory.create()

        node.accept(visitor)

        for (plugin in plugins) {
            plugin.afterRender(node, visitor)
        }

        val spanned: Spanned = visitor.builder().spannableStringBuilder()

        // clear render props and builder after rendering
        // @since 4.1.1 as we no longer reuse visitor - there is no need to clean it
        //  we might still do it if we introduce a thread-local storage though
//        visitor.clear();
        return spanned
    }

    override fun toMarkdown(input: String): Spanned {
        val spanned = render(parse(input))

        // @since 4.4.0
        // if spanned is empty, we are configured to use raw input and input is not empty
        if (TextUtils.isEmpty(spanned) && fallbackToRawInputWhenEmpty && !TextUtils.isEmpty(input)) {
            // let's use SpannableStringBuilder in order to keep backward-compatibility
            return SpannableStringBuilder(input)
        }

        return spanned
    }

    override fun setMarkdown(textView: TextView, markdown: String) {
        setParsedMarkdown(textView, toMarkdown(markdown))
    }

    override fun setParsedMarkdown(textView: TextView, markdown: Spanned) {
        for (plugin in plugins) {
            plugin.beforeSetText(textView, markdown)
        }

        // @since 4.1.0
        if (textSetter != null) {
            textSetter.setText(
                textView, markdown, bufferType
            ) { // on-complete we just must call `afterSetText` on all plugins
                for (plugin in plugins) {
                    plugin.afterSetText(textView)
                }
            }
        } else {
            // if no text-setter is specified -> just a regular sync operation

            textView.setText(markdown, bufferType)

            for (plugin in plugins) {
                plugin.afterSetText(textView)
            }
        }
    }

    override fun hasPlugin(plugin: Class<out MarkwonPlugin>): Boolean {
        return getPlugin(plugin) != null
    }

    override fun <P : MarkwonPlugin> getPlugin(type: Class<P>): P? {
        var out: MarkwonPlugin? = null
        for (plugin in plugins) {
            if (type.isAssignableFrom(plugin.javaClass)) {
                out = plugin
            }
        }
        return out as P?
    }

    override fun <P : MarkwonPlugin> requirePlugin(type: Class<P>): P {
        val plugin = getPlugin(type)
        checkNotNull(plugin) {
            String.format(
                Locale.US,
                "Requested plugin `%s` is not " + "registered with this Markwon instance",
                type.name
            )
        }
        return plugin
    }

    override fun configuration(): MarkwonConfiguration {
        return configuration
    }
}
