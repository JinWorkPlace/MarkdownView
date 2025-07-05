package com.apps.markdown.sample.samples

import com.apps.markdown.sample.BuildConfig
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.inlineparser.InlineProcessor
import io.noties.markwon.inlineparser.MarkwonInlineParser
import org.commonmark.parser.InlineParserFactory

@MarkwonSampleInfo(
    id = "20200629162023",
    title = "User mention and issue (via text)",
    description = "Github-like user mention and issue " + "rendering via `CorePlugin.OnTextAddedListener`",
    artifacts = [MarkwonArtifact.CORE, MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.PARSING, Tag.TEXT_ADDED_LISTENER, Tag.RENDERING]
)
class GithubUserIssueInlineParsingSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Custom Extension 2\n" + "\n" + "This is an issue #1\n" + "Done by @noties and other @dude"

        val inlineParserFactory: InlineParserFactory =
            MarkwonInlineParser.factoryBuilder() // include all current defaults (otherwise will be empty - contain only our inline-processors)
                //  included by default, to create factory-builder without defaults call `factoryBuilderNoDefaults`
                //                .includeDefaults()
                .addInlineProcessor(IssueInlineProcessor())
                .addInlineProcessor(UserInlineProcessor()).build()

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureParser(builder: org.commonmark.parser.Parser.Builder) {
                builder.inlineParserFactory(inlineParserFactory)
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}

internal class IssueInlineProcessor : InlineProcessor() {
    override fun specialCharacter(): Char {
        return '#'
    }

    override fun parse(): org.commonmark.node.Node? {
        val id = match(RE)
        if (id != null) {
            val link = org.commonmark.node.Link(
                createIssueOrPullRequestLinkDestination(id), null
            )
            link.appendChild(text("#$id"))
            return link
        }
        return null
    }

    companion object {
        private val RE: java.util.regex.Pattern = java.util.regex.Pattern.compile("\\d+")

        private fun createIssueOrPullRequestLinkDestination(id: String): String {
            return BuildConfig.GIT_REPOSITORY + "/issues/" + id
        }
    }
}

internal class UserInlineProcessor : InlineProcessor() {
    override fun specialCharacter(): Char {
        return '@'
    }

    override fun parse(): org.commonmark.node.Node? {
        val user = match(RE)
        if (user != null) {
            val link = org.commonmark.node.Link(
                createUserLinkDestination(user), null
            )
            link.appendChild(text("@$user"))
            return link
        }
        return null
    }

    companion object {
        private val RE: java.util.regex.Pattern = java.util.regex.Pattern.compile("\\w+")

        private fun createUserLinkDestination(user: String): String {
            return "https://github.com/$user"
        }
    }
}