package com.apps.markdown.sample.samples.html

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.LeadingMarginSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.apps.markdown.sample.BuildConfig
import com.apps.markdown.sample.R
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonSample
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.html.HtmlPlugin.HtmlConfigure
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.MarkwonHtmlRenderer
import io.noties.markwon.html.TagHandler
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.utils.LeadingMarginUtils
import io.noties.markwon.utils.NoCopySpannableFactory
import java.util.Collections

@MarkwonSampleInfo(
    id = "20200630120752",
    title = "Details HTML tag",
    description = "Handling of `details` HTML tag",
    artifacts = [MarkwonArtifact.HTML, MarkwonArtifact.IMAGE],
    tags = [Tag.IMAGE, Tag.RENDERING, Tag.HTML]
)
class HtmlDetailsSample : MarkwonSample() {
    private var context: Context? = null
    private var content: ViewGroup? = null

    override val layoutResId: Int
        get() = R.layout.sample_html_details

    override fun onViewCreated(view: View) {
        context = view.context
        content = view.findViewById<ViewGroup>(R.id.content)
        render()
    }

    private fun render() {
        val md =
            "# Hello\n\n<details>\n" + "  <summary>stuff with \n\n*mark* **down**\n\n</summary>\n" + "  <p>\n\n" + "<!-- the above p cannot start right at the beginning of the line and is mandatory for everything else to work -->\n" + "## *formatted* **heading** with [a](link)\n" + "```java\n" + "code block\n" + "```\n" + "\n" + "  <details>\n" + "    <summary><small>nested</small> stuff</summary><p>\n" + "<!-- alternative placement of p shown above -->\n" + "\n" + "* list\n" + "* with\n" + "\n\n" + "![img](" + BuildConfig.GIT_REPOSITORY + "/raw/master/art/markwon_logo.png)\n\n" + "" + " 1. nested\n" + " 1. items\n" + "\n" + "    ```java\n" + "    // including code\n" + "    ```\n" + " 1. blocks\n" + "\n" + "<details><summary>The 3rd!</summary>\n\n" + "**bold** _em_\n</details>" + "  </p></details>\n" + "</p></details>\n\n" + "and **this** *is* how..."

        val markwon: Markwon =
            Markwon.builder(context!!).usePlugin(HtmlPlugin.create(object : HtmlConfigure {
                override fun configureHtml(plugin: HtmlPlugin) {
                    plugin.addHandler(DetailsTagHandler())
                }
            })).usePlugin(ImagesPlugin.create()).build()

        val spanned: Spanned = markwon.toMarkdown(md)
        val spans: Array<DetailsParsingSpan>? =
            spanned.getSpans(0, spanned.length, DetailsParsingSpan::class.java)

        // if we have no details, proceed as usual (single text-view)
        if (spans == null || spans.isEmpty()) {
            // no details
            val textView = appendTextView()
            markwon.setParsedMarkdown(textView, spanned)
            return
        }

        val list: MutableList<DetailsElement> = ArrayList()

        for (span in spans) {
            val e: DetailsElement? = settle(
                DetailsElement(
                    spanned.getSpanStart(span), spanned.getSpanEnd(span), span.summary
                ), list
            )
            if (e != null) {
                list.add(e)
            }
        }

        for (element in list) {
            initDetails(element, spanned)
        }

        sort(list)


        var textView: TextView?
        var start = 0

        for (element in list) {
            if (element.start != start) {
                // subSequence and add new TextView
                textView = appendTextView()
                textView.text = subSequenceTrimmed(spanned, start, element.start)
            }

            // now add details TextView
            textView = appendTextView()
            initDetailsTextView(markwon, textView, element)

            start = element.end
        }

        if (start != spanned.length) {
            // another textView with rest content
            textView = appendTextView()
            textView.text = subSequenceTrimmed(spanned, start, spanned.length)
        }
    }

    private fun appendTextView(): TextView {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.view_html_details_text_view, content, false)
        val textView = view.findViewById<TextView>(R.id.text_view)
        content!!.addView(view)
        return textView
    }

    private fun initDetailsTextView(
        markwon: Markwon, textView: TextView, element: DetailsElement
    ) {
        // minor optimization

        textView.setSpannableFactory(NoCopySpannableFactory.getInstance())

        // so, each element with children is a details tag
        // there is a reason why we needed the SpannableBuilder in the first place -> we must revert spans
//        final SpannableStringBuilder builder = new SpannableStringBuilder();
        val builder: SpannableBuilder = SpannableBuilder()
        append(builder, markwon, textView, element, element)
        markwon.setParsedMarkdown(textView, builder.spannableStringBuilder())
    }

    private fun append(
        builder: SpannableBuilder,
        markwon: Markwon,
        textView: TextView,
        root: DetailsElement,
        element: DetailsElement
    ) {
        if (!element.children.isEmpty()) {
            val start: Int = builder.length

            //            builder.append(element.content);
            builder.append(subSequenceTrimmed(element.content, 0, element.content.length))

            builder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    element.expanded = !element.expanded

                    initDetailsTextView(markwon, textView, root)
                }
            }, start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            if (element.expanded) {
                for (child in element.children) {
                    append(builder, markwon, textView, root, child)
                }
            }

            builder.setSpan(DetailsSpan(markwon.configuration().theme(), element), start)
        } else {
            builder.append(element.content)
        }
    }

    private class DetailsElement(val start: Int, val end: Int, val content: CharSequence) {
        val children: MutableList<DetailsElement> = ArrayList<DetailsElement>(0)

        var expanded: Boolean = false

        override fun toString(): String {
            return "DetailsElement{" + "start=" + start + ", end=" + end + ", content=" + toStringContent(
                content
            ) + ", children=" + children + ", expanded=" + expanded + '}'
        }

        companion object {
            private fun toStringContent(cs: CharSequence): String {
                return cs.toString().replace("\n".toRegex(), "\\n")
            }
        }
    }

    private class DetailsTagHandler : TagHandler() {
        override fun handle(
            visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, tag: HtmlTag
        ) {
            var summaryEnd = -1

            for (child in tag.asBlock.children()) {
                if (!child.isClosed) {
                    continue
                }

                if ("summary" == child.name()) {
                    summaryEnd = child.end()
                }

                val tagHandler: TagHandler? = renderer.tagHandler(child.name())
                if (tagHandler != null) {
                    tagHandler.handle(visitor, renderer, child)
                } else if (child.isBlock) {
                    visitChildren(visitor, renderer, child.asBlock)
                }
            }

            if (summaryEnd > -1) {
                visitor.builder().setSpan(
                    DetailsParsingSpan(
                        subSequenceTrimmed(visitor.builder(), tag.start(), summaryEnd)
                    ), tag.start(), tag.end()
                )
            }
        }

        override fun supportedTags(): MutableCollection<String> {
            return mutableSetOf("details")
        }
    }

    private class DetailsParsingSpan(val summary: CharSequence)

    private class DetailsSpan(
        theme: MarkwonTheme, private val element: DetailsElement
    ) : LeadingMarginSpan {
        private val blockMargin: Int = theme.blockMargin
        private val blockQuoteWidth: Int = theme.blockQuoteWidth

        private val rect = Rect()
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        init {
            this.paint.style = Paint.Style.FILL
        }

        override fun getLeadingMargin(first: Boolean): Int {
            return blockMargin
        }

        override fun drawLeadingMargin(
            c: Canvas,
            p: Paint?,
            x: Int,
            dir: Int,
            top: Int,
            baseline: Int,
            bottom: Int,
            text: CharSequence?,
            start: Int,
            end: Int,
            first: Boolean,
            layout: Layout?
        ) {
            if (LeadingMarginUtils.selfStart(start, text, this)) {
                rect.set(x, top, x + blockMargin, bottom)
                if (element.expanded) {
                    paint.color = Color.GREEN
                } else {
                    paint.color = Color.RED
                }
                paint.style = Paint.Style.FILL
                c.drawRect(rect, paint)
            } else {
                if (element.expanded) {
                    val l = (blockMargin - blockQuoteWidth) / 2
                    rect.set(x + l, top, x + l + blockQuoteWidth, bottom)
                    paint.style = Paint.Style.FILL
                    paint.color = Color.GRAY
                    c.drawRect(rect, paint)
                }
            }
        }
    }

    companion object {
        // if null -> remove from where it was processed,
        // else replace from where it was processed with a new one (can become expandable)
        private fun settle(
            element: DetailsElement, elements: MutableList<out DetailsElement>
        ): DetailsElement? {
            for (e in elements) {
                if (element.start > e.start && element.end <= e.end) {
                    val settled: DetailsElement? = settle(element, e.children)
                    if (settled != null) {
                        // the thing is we must balance children if done like this
                        // let's just create a tree actually, so we are easier to modify

                        val iterator = e.children.iterator()
                        while (iterator.hasNext()) {
                            val balanced: DetailsElement? = settle(
                                iterator.next(), mutableListOf(element)
                            )
                            if (balanced == null) {
                                iterator.remove()
                            }
                        }

                        // add to our children
                        e.children.add(element)
                    }
                    return null
                }
            }
            return element
        }

        private fun initDetails(element: DetailsElement, spanned: Spanned) {
            var end = element.end
            for (i in element.children.indices.reversed()) {
                val child = element.children[i]
                if (child.end < end) {
                    element.children.add(
                        DetailsElement(
                            child.end, end, spanned.subSequence(child.end, end)
                        )
                    )
                }
                initDetails(child, spanned)
                end = child.start
            }

            val start = (element.start + element.content.length)
            if (end != start) {
                element.children.add(DetailsElement(start, end, spanned.subSequence(start, end)))
            }
        }

        private fun sort(elements: MutableList<DetailsElement>) {
            Collections.sort(
                elements, Comparator { o1: DetailsElement, o2: DetailsElement ->
                    o1.start.compareTo(o2.start)
                })
            for (element in elements) {
                sort(element.children)
            }
        }

        private fun subSequenceTrimmed(cs: CharSequence, start: Int, end: Int): CharSequence {
            var start = start
            var end = end
            while (start < end) {
                val isStartEmpty = Character.isWhitespace(cs[start])
                val isEndEmpty = Character.isWhitespace(cs[end - 1])

                if (!isStartEmpty && !isEndEmpty) {
                    break
                }

                if (isStartEmpty) {
                    start += 1
                }
                if (isEndEmpty) {
                    end -= 1
                }
            }

            return cs.subSequence(start, end)
        }
    }
}
