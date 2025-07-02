package io.noties.markwon

import android.text.Spanned
import android.widget.TextView
import io.noties.markwon.core.MarkwonTheme
import org.commonmark.node.Node
import org.commonmark.parser.Parser

/**
 * Class represents a plugin (extension) to Markwon to configure how parsing and rendering
 * of markdown is carried on.
 *
 * @see AbstractMarkwonPlugin
 *
 * @see io.noties.markwon.core.CorePlugin
 *
 * @see io.noties.markwon.movement.MovementMethodPlugin
 *
 * @since 3.0.0
 */
interface MarkwonPlugin {
    /**
     * @see Registry.require
     * @since 4.0.0
     */
    interface Action<P : MarkwonPlugin> {
        fun apply(p: P)
    }

    /**
     * @see .configure
     * @since 4.0.0
     */
    interface Registry {
        fun <P : MarkwonPlugin> require(plugin: Class<P>): P

        fun <P : MarkwonPlugin> require(
            plugin: Class<P>, action: Action<in P>
        )
    }

    /**
     * This method will be called before any other during [Markwon] instance construction.
     *
     * @since 4.0.0
     */
    fun configure(registry: Registry)

    /**
     * Method to configure `org.commonmark.parser.Parser` (for example register custom
     * extension, etc).
     */
    fun configureParser(builder: Parser.Builder)

    /**
     * Modify [MarkwonTheme] that is used for rendering of markdown.
     *
     * @see MarkwonTheme
     *
     * @see MarkwonTheme.Builder
     */
    fun configureTheme(builder: MarkwonTheme.Builder)

    /**
     * Configure [MarkwonConfiguration]
     *
     * @see MarkwonConfiguration
     *
     * @see MarkwonConfiguration.Builder
     */
    fun configureConfiguration(builder: MarkwonConfiguration.Builder)

    /**
     * Configure [MarkwonVisitor] to accept new node types or override already registered nodes.
     *
     * @see MarkwonVisitor
     *
     * @see MarkwonVisitor.Builder
     */
    fun configureVisitor(builder: MarkwonVisitor.Builder)

    /**
     * Configure [MarkwonSpansFactory] to change what spans are used for certain node types.
     *
     * @see MarkwonSpansFactory
     *
     * @see MarkwonSpansFactory.Builder
     */
    fun configureSpansFactory(builder: MarkwonSpansFactory.Builder)

    /**
     * Process input markdown and return new string to be used in parsing stage further.
     * Can be described as `pre-processing` of markdown String.
     *
     * @param markdown String to process
     * @return processed markdown String
     */
    fun processMarkdown(markdown: String): String

    /**
     * This method will be called **before** rendering will occur thus making possible
     * to `post-process` parsed node (make changes for example).
     *
     * @param node root parsed org.commonmark.node.Node
     */
    fun beforeRender(node: Node)

    /**
     * This method will be called **after** rendering (but before applying markdown to a
     * TextView, if such action will happen). It can be used to clean some
     * internal state, or trigger certain action. Please note that modifying `node` won\'t
     * have any effect as it has been already *visited* at this stage.
     *
     * @param node    root parsed org.commonmark.node.Node
     * @param visitor [MarkwonVisitor] instance used to render markdown
     */
    fun afterRender(node: Node, visitor: MarkwonVisitor)

    /**
     * This method will be called **before** calling `TextView#setText`.
     *
     *
     * It can be useful to prepare a TextView for markdown. For example `ru.noties.markwon.image.ImagesPlugin`
     * uses this method to unregister previously registered [io.noties.markwon.image.AsyncDrawableSpan]
     * (if there are such spans in this TextView at this point). Or [io.noties.markwon.core.CorePlugin]
     * which measures ordered list numbers
     *
     * @param textView TextView to which `markdown` will be applied
     * @param markdown Parsed markdown
     */
    fun beforeSetText(textView: TextView, markdown: Spanned)

    /**
     * This method will be called **after** markdown was applied.
     *
     *
     * It can be useful to trigger certain action on spans/textView. For example `ru.noties.markwon.image.ImagesPlugin`
     * uses this method to register [io.noties.markwon.image.AsyncDrawableSpan] and start
     * asynchronously loading images.
     *
     *
     * Unlike [.beforeSetText] this method does not receive parsed markdown
     * as at this point spans must be queried by calling `TextView#getText#getSpans`.
     *
     * @param textView TextView to which markdown was applied
     */
    fun afterSetText(textView: TextView)
}
