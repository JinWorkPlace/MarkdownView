package com.apps.markdown.sample.samples.editor

import android.graphics.Paint
import android.text.method.LinkMovementMethod
import android.text.style.ReplacementSpan
import android.view.View
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.samples.editor.shared.BlockQuoteEditHandler
import com.apps.markdown.sample.samples.editor.shared.CodeEditHandler
import com.apps.markdown.sample.samples.editor.shared.HeadingEditHandler
import com.apps.markdown.sample.samples.editor.shared.LinkEditHandler
import com.apps.markdown.sample.samples.editor.shared.MarkwonEditTextSample
import com.apps.markdown.sample.samples.editor.shared.StrikethroughEditHandler
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import io.noties.markwon.editor.handler.EmphasisEditHandler
import io.noties.markwon.editor.handler.StrongEmphasisEditHandler

@MarkwonSampleInfo(
    id = "20200908133515",
    title = "WYSIWG editor",
    description = "A possible direction to implement what-you-see-is-what-you-get editor",
    artifacts = [MarkwonArtifact.EDITOR],
    tags = [Tag.RENDERING]
)
class WYSIWYGEditorSample : MarkwonEditTextSample() {
    override fun render() {
        // when automatic line break is inserted and text is inside margin span (blockquote, list, etc)
        //  be prepared to encounter selection bugs (selection would be drawn at the place as is no margin
        //  span is present)

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(SoftBreakAddsNewLinePlugin.create()).build()

        val editor: MarkwonEditor = MarkwonEditor.builder(markwon).punctuationSpan(
            HidePunctuationSpan::class.java,
            object : io.noties.markwon.editor.PersistedSpans.SpanFactory<HidePunctuationSpan> {
                override fun create(): HidePunctuationSpan {
                    return HidePunctuationSpan()
                }
            }).useEditHandler(EmphasisEditHandler()).useEditHandler(StrongEmphasisEditHandler())
            .useEditHandler(StrikethroughEditHandler()).useEditHandler(CodeEditHandler())
            .useEditHandler(BlockQuoteEditHandler())
            .useEditHandler(LinkEditHandler(object : LinkEditHandler.OnClick {
                override fun onClick(widget: View, link: String) {
                }
            })).useEditHandler(HeadingEditHandler()).build()

        // for links to be clickable
        //   NB! markwon MovementMethodPlugin cannot be used here as editor do not execute `beforeSetText`)
        editText.movementMethod = LinkMovementMethod.getInstance()

        editText.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor))
    }

    private class HidePunctuationSpan : ReplacementSpan() {
        override fun getSize(
            paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?
        ): Int {
            // last space (which is swallowed until next non-space character appears)
            // block quote
            // code tick

//      Debug.i("text: '%s', %d-%d (%d)", text.subSequence(start, end), start, end, text.length());

            if (end == text.length) {
                // TODO: find first non-space character (not just first one because commonmark allows
                //  arbitrary (0-3) white spaces before content starts

                //  TODO: if all white space - render?

                val c = text[start]
                if ('#' == c || '>' == c || '-' == c // TODO: not thematic break
                    || '+' == c // `*` is fine but only for a list
                    || isBulletList(
                        text, c, start, end
                    ) || Character.isDigit(c) // assuming ordered list (replacement should only happen for ordered lists)
                    || Character.isWhitespace(c)
                ) {
                    return (paint.measureText(text, start, end) + 0.5f).toInt()
                }
            }
            return 0
        }

        override fun draw(
            canvas: android.graphics.Canvas,
            text: CharSequence,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint
        ) {
            // will be called only when getSize is not 0 (and if it was once reported as 0...)
            if (end == text.length) {
                // if first non-space is `*` then check for is bullet
                //  else `**` would be still rendered at the end of the emphasis

                if (text[start] == '*' && !isBulletList(
                        text, '*', start, end
                    )
                ) {
                    return
                }

                // TODO: inline code last tick received here, handle it (do not highlight)
                //  why can't we have reported width in this method for supplied text?

                // let's use color to make it distinct from the rest of the text for demonstration purposes
                paint.color = -0x10000

                canvas.drawText(text, start, end, x, y.toFloat(), paint)
            }
        }

        companion object {
            private fun isBulletList(
                text: CharSequence, firstChar: Char, start: Int, end: Int
            ): Boolean {
                return '*' == firstChar && ((end - start == 1) || (Character.isWhitespace(
                    text[start + 1]
                )))
            }
        }
    }
}
