package com.apps.markdown.sample.samples

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ReplacementSpan
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.image.ImagesPlugin

@MarkwonSampleInfo(
    id = "20200629161505",
    title = "Read more plugin",
    description = "Plugin that adds expand/collapse (\"show all\"/\"show less\")",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.PLUGIN]
)
class ReadMorePluginSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "Lorem **ipsum** ![dolor](https://avatars2.githubusercontent.com/u/30618885?s=460&v=4) sit amet, consectetur adipiscing elit. Morbi vitae enim ut sem aliquet ultrices. Nunc a accumsan orci. Suspendisse tortor ante, lacinia ac scelerisque sed, dictum eget metus. Morbi ante augue, tristique eget quam in, vestibulum rutrum lacus. Nulla aliquam auctor cursus. Nulla at lacus condimentum, viverra lacus eget, sollicitudin ex. Cras efficitur leo dui, sit amet rutrum tellus venenatis et. Sed in facilisis libero. Etiam ultricies, nulla ut venenatis tincidunt, tortor erat tristique ante, non aliquet massa arcu eget nisl. Etiam gravida erat ante, sit amet lobortis mauris commodo nec. Praesent vitae sodales quam. Vivamus condimentum porta suscipit. Donec posuere id felis ac scelerisque. Vestibulum lacinia et leo id lobortis. Sed vitae dolor nec ligula dapibus finibus vel eu libero. Nam tincidunt maximus elit, sit amet tincidunt lacus laoreet malesuada.\n\n" + "here we ![are](https://avatars2.githubusercontent.com/u/30618885?s=460&v=4)"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(ImagesPlugin.create()).usePlugin(ReadMorePlugin())
                .build()

        markwon.setMarkdown(textView, md)
    }
}

/**
 * Read more plugin based on text length. It is easier to implement than lines (need to adjust
 * last line to include expand/collapse text).
 */
internal class ReadMorePlugin : AbstractMarkwonPlugin() {
    private val maxLength = 150

    private val labelMore = "Show more..."

    private val labelLess = "...Show less"

    override fun configure(registry: io.noties.markwon.MarkwonPlugin.Registry) {
        // establish connections with all _dynamic_ content that your markdown supports,
        //  like images, tables, latex, etc
        registry.require(ImagesPlugin::class.java)
    }

    override fun afterSetText(textView: android.widget.TextView) {
        val text = textView.text
        if (text.length < maxLength) {
            // everything is OK, no need to ellipsize)
            return
        }

        val breakAt: Int = breakTextAt(text, 0, maxLength)
        val cs = createCollapsedString(text, 0, breakAt)
        textView.text = cs
    }

    private fun createCollapsedString(
        text: CharSequence, start: Int, end: Int
    ): CharSequence {
        val builder = SpannableStringBuilder(text, start, end)

        // NB! each table row is represented as a space character and new-line (so length=2) no
        //  matter how many characters are inside table cells

        // we can _clean_ this builder, for example remove all dynamic content (like images and tables,
        //  but keep them in full/expanded version)
        // it is an implementation detail but _mostly_ dynamic content is implemented as
        //  ReplacementSpans
        val spans: Array<ReplacementSpan> =
            builder.getSpans(0, builder.length, ReplacementSpan::class.java)
        for (span in spans) {
            builder.removeSpan(span)
        }

        // NB! if there will be a table in _preview_ (collapsed) then each row will be represented as a
        // space and new-line
        trim(builder)

        val fullText = createFullText(text, builder)

        builder.append(' ')

        val length: Int = builder.length
        builder.append(labelMore)
        builder.setSpan(object : ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                (widget as android.widget.TextView).text = fullText
            }
        }, length, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return builder
    }

    private fun createFullText(
        text: CharSequence, collapsedText: CharSequence
    ): CharSequence {
        // full/expanded text can also be different,
        //  for example it can be kept as-is and have no `collapse` functionality (once expanded cannot collapse)
        //  or can contain collapse feature
        val fullText: CharSequence
        // for example let's allow collapsing
        val builder = SpannableStringBuilder(text)
        builder.append(' ')

        val length: Int = builder.length
        builder.append(labelLess)
        builder.setSpan(object : ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                (widget as android.widget.TextView).text = collapsedText
            }
        }, length, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        fullText = builder

        return fullText
    }

    companion object {
        private fun trim(builder: SpannableStringBuilder) {
            // NB! tables use `\u00a0` (non breaking space) which is not reported as white-space

            var c: Char

            run {
                var i = 0
                val length: Int = builder.length
                while (i < length) {
                    c = builder[i]
                    if (!Character.isWhitespace(c) && c != '\u00a0') {
                        if (i > 0) {
                            builder.replace(0, i, "")
                        }
                        break
                    }
                    i++
                }
            }

            for (i in builder.length - 1 downTo 0) {
                c = builder.get(i)
                if (!Character.isWhitespace(c) && c != '\u00a0') {
                    if (i < builder.length - 1) {
                        builder.replace(i, builder.length, "")
                    }
                    break
                }
            }
        }

        // depending on your locale these can be different
        // There is a BreakIterator in Android, but it is not reliable, still theoretically
        //  it should work better than hand-written and hardcoded rules
        private fun breakTextAt(
            text: CharSequence, start: Int, max: Int
        ): Int {
            var last = start

            // no need to check for _start_ (anyway will be ignored)
            for (i in start + max - 1 downTo start + 1) {
                val c = text[i]
                if (Character.isWhitespace(c) || c == '.' || c == ',' || c == '!' || c == '?') {
                    // include this special character
                    last = i - 1
                    break
                }
            }

            if (last <= start) {
                // when used in subSequence last index is exclusive,
                //  so given max=150 would result in 0-149 subSequence
                return start + max
            }

            return last
        }
    }
}

