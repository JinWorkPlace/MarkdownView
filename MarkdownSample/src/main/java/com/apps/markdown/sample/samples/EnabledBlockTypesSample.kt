import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import org.commonmark.node.BlockQuote
import org.commonmark.parser.Parser

@MarkwonSampleInfo(
    id = "20200627075012",
    title = "Enabled markdown blocks",
    description = "Modify/inspect enabled by `CorePlugin` block types. " + "Disable quotes or other blocks from being parsed",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.PARSING, Tag.BLOCK, Tag.PLUGIN]
)
class EnabledBlockTypesSample : MarkwonTextViewSample() {
    override fun render() {
        val md = """
      # Heading
      ## Second level
      > Quote is not handled
    """.trimIndent()

        val markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureParser(builder: Parser.Builder) {
                // obtain all enabled block types
                val enabledBlockTypes = CorePlugin.enabledBlockTypes()
                // it is safe to modify returned collection
                // remove quotes
                enabledBlockTypes.remove(BlockQuote::class.java)

                builder.enabledBlockTypes(enabledBlockTypes)
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}