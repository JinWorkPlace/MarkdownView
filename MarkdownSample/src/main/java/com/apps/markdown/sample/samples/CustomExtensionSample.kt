package com.apps.markdown.sample.samples

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.text.style.ReplacementSpan
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.CustomNode
import org.commonmark.node.Delimited
import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun
import java.util.regex.Matcher
import java.util.regex.Pattern

@MarkwonSampleInfo(
    id = "20200629163248",
    title = "Custom extension",
    description = "Custom extension that adds an " + "icon from resources and renders it as image with " + "`@ic-name` syntax",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.PARSING, Tag.RENDERING, Tag.PLUGIN, Tag.IMAGE, Tag.EXTENSION, Tag.SPAN]
)
class CustomExtensionSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Hello! @ic-android-black-24\n\n" + "" + "Home 36 black: @ic-home-black-36\n\n" + "" + "Memory 48 black: @ic-memory-black-48\n\n" + "" + "### I AM ANOTHER HEADER\n\n" + "" + "Sentiment Satisfied 64 red: @ic-sentiment_satisfied-red-64" + ""

        // note that we haven't registered CorePlugin, as it's the only one that can be
        // implicitly deducted and added automatically. All other plugins require explicit
        // `usePlugin` call
        val markwon: Markwon = Markwon.builder(context)
            .usePlugin(IconPlugin.Companion.create(IconSpanProvider.Companion.create(context, 0)))
            .build()

        markwon.setMarkdown(textView, md)
    }
}

internal class IconPlugin(
    private val iconSpanProvider: IconSpanProvider
) : AbstractMarkwonPlugin() {
    override fun configureParser(builder: Parser.Builder) {
        builder.customDelimiterProcessor(IconProcessor.Companion.create())
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(
            IconNode::class.java,
            MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor, iconNode: IconNode ->
                val name = iconNode.name()
                val color = iconNode.color()
                val size = iconNode.size()
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(color) && !TextUtils.isEmpty(size)) {
                    val length: Int = visitor.length()

                    visitor.builder().append(name)
                    visitor.setSpans(length, iconSpanProvider.provide(name, color, size))
                    visitor.builder().append(' ')
                }
            })
    }

    override fun processMarkdown(markdown: String): String {
        return IconProcessor.Companion.prepare(markdown)
    }

    companion object {
        fun create(iconSpanProvider: IconSpanProvider): IconPlugin {
            return IconPlugin(iconSpanProvider)
        }
    }
}

internal abstract class IconSpanProvider {
    abstract fun provide(name: String, color: String, size: String): IconSpan


    private class Impl(private val context: Context, @param:DrawableRes private val fallBack: Int) :
        IconSpanProvider() {
        private val resources: Resources = context.resources

        override fun provide(name: String, color: String, size: String): IconSpan {
            val resName: String = iconName(name, color, size)
            var resId = resources.getIdentifier(resName, "drawable", context.packageName)
            if (resId == 0) {
                resId = fallBack
            }
            return IconSpan(getDrawable(resId), IconSpan.Companion.ALIGN_CENTER)
        }


        fun getDrawable(resId: Int): Drawable {
            return context.getDrawable(resId)!!
        }

        companion object {
            private fun iconName(name: String, color: String, size: String): String {
                return "ic_" + name + "_" + color + "_" + size + "dp"
            }
        }
    }

    companion object {
        fun create(context: Context, @DrawableRes fallBack: Int): IconSpanProvider {
            return Impl(context, fallBack)
        }
    }
}

internal class IconSpan(
    private val drawable: Drawable, @param:Alignment private val alignment: Int
) : ReplacementSpan() {
    @IntDef(ALIGN_BOTTOM, ALIGN_BASELINE, ALIGN_CENTER)
    @Retention(AnnotationRetention.BINARY)
    internal annotation class Alignment

    init {
        if (drawable.bounds.isEmpty) {
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        }
    }

    override fun getSize(
        paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?
    ): Int {
        val rect = drawable.bounds

        if (fm != null) {
            fm.ascent = -rect.bottom
            fm.descent = 0

            fm.top = fm.ascent
            fm.bottom = 0
        }

        return rect.right
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val b = bottom - drawable.bounds.bottom

        val save = canvas.save()
        try {
            val translationY: Int = if (ALIGN_CENTER == alignment) {
                b - ((bottom - top - drawable.bounds.height()) / 2)
            } else if (ALIGN_BASELINE == alignment) {
                b - paint.fontMetricsInt.descent
            } else {
                b
            }
            canvas.translate(x, translationY.toFloat())
            drawable.draw(canvas)
        } finally {
            canvas.restoreToCount(save)
        }
    }

    companion object {
        const val ALIGN_BOTTOM: Int = 0
        const val ALIGN_BASELINE: Int = 1
        const val ALIGN_CENTER: Int =
            2 // will only center if drawable height is less than text line height
    }
}

internal class IconProcessor : DelimiterProcessor {
    override fun getOpeningCharacter(): Char = IconNode.Companion.DELIMITER

    override fun getClosingCharacter(): Char = IconNode.Companion.DELIMITER

    override fun getMinLength(): Int = 1

    override fun getDelimiterUse(opener: DelimiterRun, closer: DelimiterRun): Int {
        return if (opener.length() >= 1 && closer.length() >= 1) 1 else 0
    }

    override fun process(opener: Text, closer: Text?, delimiterUse: Int) {
        val iconGroupNode = IconGroupNode()

        val next = opener.next

        var handled = false

        // process only if we have exactly one Text node
        if (next is Text && next.next === closer) {
            val text = next.literal

            if (!TextUtils.isEmpty(text)) {
                // attempt to match

                val matcher: Matcher = PATTERN.matcher(text)
                if (matcher.matches()) {
                    val iconNode = IconNode(matcher.group(1)!!, matcher.group(2)!!, matcher.group(3)!!)
                    iconGroupNode.appendChild(iconNode)
                    next.unlink()
                    handled = true
                }
            }
        }

        if (!handled) {
            // restore delimiters if we didn't match

            iconGroupNode.appendChild(Text(IconNode.Companion.closingDelimiter))

            var node: Node?
            var tmp = opener.next
            while (tmp != null && tmp !== closer) {
                node = tmp.next
                // append a child anyway
                iconGroupNode.appendChild(tmp)
                tmp = node
            }

            iconGroupNode.appendChild(Text(IconNode.Companion.closingDelimiter))
        }

        opener.insertBefore(iconGroupNode)
    }

    companion object {
        fun create(): IconProcessor {
            return IconProcessor()
        }

        // ic-home-black-24
        private val PATTERN: Pattern = Pattern.compile("ic-(\\w+)-(\\w+)-(\\d+)")

        private const val TO_FIND: String = IconNode.Companion.closingDelimiter + "ic-"

        /**
         * Should be used when input string does not wrap icon definition with `@` from both ends.
         * So, `@ic-home-white-24` would become `@ic-home-white-24@`. This way parsing is easier
         * and more predictable (cannot specify multiple ending delimiters, as we would require them:
         * space, newline, end of a document, and a lot of more)
         *
         * @param input to process
         * @return processed string
         * @see .prepare
         */
        fun prepare(input: String): String {
            val builder = StringBuilder(input)
            prepare(builder)
            return builder.toString()
        }

        fun prepare(builder: StringBuilder) {
            var start = builder.indexOf(TO_FIND)
            var end: Int

            while (start > -1) {
                end = iconDefinitionEnd(start + TO_FIND.length, builder)

                // if we match our pattern, append `@` else ignore
                if (iconDefinitionValid(builder.subSequence(start + 1, end))) {
                    builder.insert(end, '@')
                }

                // move to next
                start = builder.indexOf(TO_FIND, end)
            }
        }

        private fun iconDefinitionEnd(index: Int, builder: StringBuilder): Int {
            // all spaces, new lines, non-words or digits,

            var c: Char

            var end = -1
            for (i in index..<builder.length) {
                c = builder.get(i)
                if (Character.isWhitespace(c) || !(Character.isLetterOrDigit(c) || c == '-' || c == '_')) {
                    end = i
                    break
                }
            }

            if (end == -1) {
                end = builder.length
            }

            return end
        }

        private fun iconDefinitionValid(cs: CharSequence): Boolean {
            val matcher: Matcher = PATTERN.matcher(cs)
            return matcher.matches()
        }
    }
}

internal class IconNode(
    private val name: String, private val color: String, private val size: String
) : CustomNode(), Delimited {
    fun name(): String {
        return name
    }

    fun color(): String {
        return color
    }

    fun size(): String {
        return size
    }

    override fun toString(): String {
        return "IconNode{name='$name', color='$color', size='$size'}"
    }

    override fun getOpeningDelimiter(): String = closingDelimiter

    override fun getClosingDelimiter(): String = closingDelimiter

    companion object {
        const val DELIMITER: Char = '@'

        const val closingDelimiter: String = " $DELIMITER"

    }
}

internal class IconGroupNode : CustomNode()
