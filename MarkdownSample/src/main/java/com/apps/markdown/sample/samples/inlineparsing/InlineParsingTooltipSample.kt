package com.apps.markdown.sample.samples.inlineparsing

import android.app.Activity
import android.graphics.Point
import android.text.Spannable
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.inlineparser.InlineProcessor
import io.noties.markwon.inlineparser.MarkwonInlineParser
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import org.commonmark.node.CustomNode
import org.commonmark.node.Node
import java.util.regex.Matcher
import java.util.regex.Pattern

@MarkwonSampleInfo(
    id = "20200630195409",
    title = "Tooltip with inline parser",
    artifacts = [MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.PARSING, Tag.RENDERING]
)
class InlineParsingTooltipSample : MarkwonTextViewSample() {
    override fun render() {
        // NB! tooltip contents cannot have new lines
        val md =
            "" + "\n" + "\n" + "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi vitae enim ut sem aliquet ultrices. Nunc a accumsan orci. Suspendisse tortor ante, lacinia ac scelerisque sed, dictum eget metus. Morbi ante augue, tristique eget quam in, vestibulum rutrum lacus. Nulla aliquam auctor cursus. Nulla at lacus condimentum, viverra lacus eget, sollicitudin ex. Cras efficitur leo dui, sit amet rutrum tellus venenatis et. Sed in facilisis libero. Etiam ultricies, nulla ut venenatis tincidunt, tortor erat tristique ante, non aliquet massa arcu eget nisl. Etiam gravida erat ante, sit amet lobortis mauris commodo nec. Praesent vitae sodales quam. Vivamus condimentum porta suscipit. Donec posuere id felis ac scelerisque. Vestibulum lacinia et leo id lobortis. Sed vitae dolor nec ligula dapibus finibus vel eu libero. Nam tincidunt maximus elit, sit amet tincidunt lacus laoreet malesuada.\n" + "\n" + "Aenean at urna leo. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nulla facilisi. Integer lectus elit, congue a orci sed, dignissim sagittis sem. Aenean et pretium magna, nec ornare justo. Sed quis nunc blandit, luctus justo eget, pellentesque arcu. Pellentesque porta semper tristique. Donec et odio arcu. Nullam ultrices gravida congue. Praesent vel leo sed orci tempor luctus. Vivamus eget tortor arcu. Nullam sapien nulla, iaculis sit amet semper in, mattis nec metus. In porttitor augue id elit euismod mattis. Ut est justo, dapibus suscipit erat eu, pellentesque porttitor magna.\n" + "\n" + "Nunc porta orci eget dictum malesuada. Donec vehicula felis sit amet leo tincidunt placerat. Cras quis elit faucibus, porta elit at, sodales tortor. Donec elit mi, eleifend et maximus vitae, pretium varius velit. Integer maximus egestas urna, at semper augue egestas vitae. Phasellus arcu tellus, tincidunt eget tellus nec, hendrerit mollis mauris. Pellentesque commodo urna quis nisi ultrices, quis vehicula felis ultricies. Vivamus eu feugiat leo.\n" + "\n" + "Etiam sit amet lorem et eros suscipit rhoncus a a tellus. Sed pharetra dui purus, quis molestie leo congue nec. Suspendisse sed scelerisque quam. Vestibulum non laoreet felis. Fusce interdum euismod purus at scelerisque. Vivamus tempus varius nibh, sed accumsan nisl interdum non. Pellentesque rutrum egestas eros sit amet sollicitudin. Vivamus ultrices est erat. Curabitur gravida justo non felis euismod mollis. Ut porta finibus nulla, sed pellentesque purus euismod ac.\n" + "\n" + "Aliquam erat volutpat. Nullam suscipit sit amet tortor vel fringilla. Nulla facilisi. Nullam lacinia ex lacus, sit amet scelerisque justo semper a. Nullam ullamcorper, erat ac malesuada porta, augue erat sagittis mi, in auctor turpis mauris nec orci. Nunc sit amet felis placerat, pharetra diam nec, dapibus metus. Proin nulla orci, iaculis vitae vulputate vel, placerat ac erat. Morbi sit amet blandit velit. Cras consectetur vehicula lacus vel sagittis. Nunc tincidunt lacus in blandit faucibus. Curabitur vestibulum auctor vehicula. Sed quis ligula sit amet quam venenatis venenatis eget id felis. Maecenas feugiat nisl elit, facilisis tempus risus malesuada quis. " + "# Hello tooltip!\n\n" + "This is the !{tooltip label}(and actual content comes here)\n\n" + "what if it is !{here}(The contents can be blocks, limited though) instead?\n\n" + "![image](https://github.com/dcurtis/markdown-mark/raw/master/png/208x128-solid.png) anyway"

        val markwon: Markwon = Markwon.builder(context)
            .usePlugin(MarkwonInlineParserPlugin.create(object :
                MarkwonInlineParserPlugin.BuilderConfigure<MarkwonInlineParser.FactoryBuilder> {
                override fun configureBuilder(factoryBuilder: MarkwonInlineParser.FactoryBuilder) {
                    factoryBuilder.addInlineProcessor(
                        TooltipInlineProcessor()
                    )
                }
            })).usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    builder.on(
                        TooltipNode::class.java,
                        MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor, tooltipNode: TooltipNode ->
                            val start: Int = visitor.length()
                            visitor.builder().append(tooltipNode.label)
                            visitor.setSpans(start, TooltipSpan(tooltipNode.contents))
                        })
                }
            }).usePlugin(ImagesPlugin.create()).build()

        markwon.setMarkdown(textView, md)
    }
}

internal class TooltipInlineProcessor : InlineProcessor() {
    override fun specialCharacter(): Char {
        return '!'
    }

    override fun parse(): Node? {
        val match = match(RE)
        if (match == null) {
            return null
        }

        val matcher: Matcher = RE.matcher(match)
        if (matcher.matches()) {
            val label = matcher.group(1)
            val contents = matcher.group(2)
            return TooltipNode(label!!, contents!!)
        }

        return null
    }

    companion object {
        // NB! without bang
        // `\\{` is required (although marked as redundant), without it - runtime crash
        private val RE: Pattern = Pattern.compile("\\{(.+?)\\}\\((.+?)\\)")
    }
}

internal class TooltipNode(val label: String, val contents: String) : CustomNode()

internal class TooltipSpan(val contents: String) : ClickableSpan() {
    override fun onClick(widget: View) {
        // just to be safe
        if (widget !is TextView) {
            return
        }

        val textView = widget
        val spannable: Spannable = textView.text as Spannable

        // find self ending position (can also obtain start)
//    final int start = spannable.getSpanStart(this);
        val end: Int = spannable.getSpanEnd(this)

        // weird, didn't find self
        if ( /*start < 0 ||*/end < 0) {
            return
        }

        val layout = textView.layout
        if (layout == null) {
            // also weird
            return
        }

        val line = layout.getLineForOffset(end)

        // position inside TextView, these values must also be adjusted to parent widget
        // also note that container can
        val y = layout.getLineBottom(line)
        val x = (layout.getPrimaryHorizontal(end) + 0.5f).toInt()

        val window = (widget.context as Activity).window
        val decor = window.decorView
        val point = relativeTo(decor, widget)
        val toast: Toast = Toast.makeText(widget.context, contents, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.TOP or Gravity.START, x + point.x, y + point.y)
        toast.show()
    }

    override fun updateDrawState(ds: TextPaint) {
        // can customize appearance here as spans will be rendered as links
        super.updateDrawState(ds)
    }

    companion object {
        private fun relativeTo(parent: View, who: View, point: Point = Point()): Point {
            // NB! the scroll adjustments (we are interested in screen position,
            //  not real position inside parent)
            point.x += who.left
            point.y += who.top
            point.x -= who.scrollX
            point.y -= who.scrollY
            if (who !== parent && who.parent is View) {
                relativeTo(parent, (who.parent as View?)!!, point)
            }
            return point
        }
    }
}
