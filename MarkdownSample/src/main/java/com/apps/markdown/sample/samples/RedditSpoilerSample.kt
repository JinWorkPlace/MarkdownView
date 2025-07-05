package com.apps.markdown.sample.samples

import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.ClickableSpan
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon

@MarkwonSampleInfo(
    id = "20200813145316",
    title = "Reddit spoiler",
    description = "An attempt to implement Reddit spoiler syntax `>! !<`",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.PARSING]
)
class RedditSpoilerSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Reddit spolier\n\n" + "Hello >!ugly so **ugly** !<, how are you?\n\n" + ">!a blockquote?!< should not be >!present!< yeah" + ""

        val markwon: Markwon = Markwon.builder(context).usePlugin(RedditSpoilerPlugin()).build()

        markwon.setMarkdown(textView, md)
    }
}

internal class RedditSpoilerPlugin : AbstractMarkwonPlugin() {
    override fun processMarkdown(markdown: String): String {
        // replace all `>!` with `&gt;!` so no blockquote would be parsed (when spoiler starts at new line)
        return markdown.replace(">!".toRegex(), "&gt;!")
    }

    override fun beforeSetText(textView: android.widget.TextView, markdown: Spanned) {
        applySpoilerSpans(markdown as Spannable)
    }

    private class RedditSpoilerSpan : CharacterStyle() {
        private var revealed = false

        override fun updateDrawState(tp: TextPaint) {
            if (!revealed) {
                // use the same text color
                tp.bgColor = android.graphics.Color.BLACK
                tp.color = android.graphics.Color.BLACK
            } else {
                // for example keep a bit of black background to remind that it is a spoiler
                tp.bgColor =
                    io.noties.markwon.utils.ColorUtils.applyAlpha(android.graphics.Color.BLACK, 25)
            }
        }

        fun setRevealed(revealed: Boolean) {
            this.revealed = revealed
        }
    }

    // we also could make text size smaller (but then MetricAffectingSpan should be used)
    private class HideSpoilerSyntaxSpan : CharacterStyle() {
        override fun updateDrawState(tp: TextPaint) {
            // set transparent color
            tp.color = 0
        }
    }

    companion object {
        private val RE: java.util.regex.Pattern = java.util.regex.Pattern.compile(">!.+?!<")

        private fun applySpoilerSpans(spannable: Spannable) {
            val text = spannable.toString()
            val matcher: java.util.regex.Matcher = RE.matcher(text)

            while (matcher.find()) {
                val spoilerSpan = RedditSpoilerSpan()
                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: android.view.View) {
                        spoilerSpan.setRevealed(true)
                        widget.postInvalidateOnAnimation()
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        // no op
                    }
                }

                val s = matcher.start()
                val e = matcher.end()
                spannable.setSpan(spoilerSpan, s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(clickableSpan, s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                // we also can hide original syntax
                spannable.setSpan(
                    HideSpoilerSyntaxSpan(), s, s + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    HideSpoilerSyntaxSpan(), e - 2, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
}