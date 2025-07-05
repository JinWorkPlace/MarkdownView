//package com.apps.markdown.sample.samples
//
//import com.apps.markdown.sample.annotations.MarkwonArtifact
//import com.apps.markdown.sample.annotations.MarkwonSampleInfo
//import com.apps.markdown.sample.annotations.Tag
//import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
//import io.noties.markwon.AbstractMarkwonPlugin
//import io.noties.markwon.Markwon
//import io.noties.markwon.MarkwonConfiguration
//import io.noties.markwon.MarkwonVisitor
//import io.noties.markwon.RenderProps
//import io.noties.markwon.SpannableBuilder
//import io.noties.markwon.core.CorePlugin
//import io.noties.markwon.core.CoreProps
//import org.commonmark.node.Link
//
//@MarkwonSampleInfo(
//    id = "20200629162024",
//    title = "User mention and issue (via text)",
//    description = "Github-like user mention and issue " + "rendering via `CorePlugin.OnTextAddedListener`",
//    artifacts = [MarkwonArtifact.CORE],
//    tags = [Tag.PARSING, Tag.TEXT_ADDED_LISTENER, Tag.RENDERING]
//)
//class GithubUserIssueOnTextAddedSample : MarkwonTextViewSample() {
//    override fun render() {
//        val md =
//            "" + "# Custom Extension 2\n" + "\n" + "This is an issue #1\n" + "Done by @noties and other @dude"
//
//        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
//            override fun configure(registry: io.noties.markwon.MarkwonPlugin.Registry) {
//                registry.require(
//                    CorePlugin::class.java,
//                    io.noties.markwon.MarkwonPlugin.Action { corePlugin: CorePlugin ->
//                        corePlugin.addOnTextAddedListener(GithubLinkifyRegexTextAddedListener())
//                    })
//            }
//        }).build()
//
//        markwon.setMarkdown(textView, md)
//    }
//}
//
//internal class GithubLinkifyRegexTextAddedListener : CorePlugin.OnTextAddedListener {
//    override fun onTextAdded(visitor: MarkwonVisitor, text: String, start: Int) {
//        val matcher: java.util.regex.Matcher = PATTERN.matcher(text)
//
//        var value: String?
//        var url: String?
//        var index: Int
//
//        while (matcher.find()) {
//            value = matcher.group(1)
//
//            // detect which one it is
//            if ('#' == value[0]) {
//                url = createIssueOrPullRequestLink(value.substring(1))
//            } else {
//                url = createUserLink(value.substring(1))
//            }
//
//            // it's important to use `start` value (represents start-index of `text` in the visitor)
//            index = start + matcher.start()
//
//            setLink(visitor, url, index, index + value.length)
//        }
//    }
//
//    private fun createIssueOrPullRequestLink(number: String): String {
//        // issues and pull-requests on github follow the same pattern and we
//        // cannot know for sure which one it is, but if we use issues for all types,
//        // github will automatically redirect to pull-request if it's the one which is opened
//        return BuildConfig.GIT_REPOSITORY + "/issues/" + number
//    }
//
//    private fun createUserLink(user: String): String {
//        return "https://github.com/$user"
//    }
//
//    private fun setLink(
//        visitor: MarkwonVisitor, destination: String, start: Int, end: Int
//    ) {
//        // might a simpler one, but it doesn't respect possible changes to links
////            visitor.builder().setSpan(
////                    new LinkSpan(visitor.configuration().theme(), destination, visitor.configuration().linkResolver()),
////                    start,
////                    end
////            );
//
//        // use default handlers for links
//
//        val configuration: MarkwonConfiguration = visitor.configuration()
//        val renderProps: RenderProps = visitor.renderProps()
//
//        CoreProps.LINK_DESTINATION.set(renderProps, destination)
//
//        SpannableBuilder.setSpans(
//            visitor.builder(),
//            configuration.spansFactory().require(Link::class.java)
//                .getSpans(configuration, renderProps),
//            start,
//            end
//        )
//    }
//
//    companion object {
//        private val PATTERN: java.util.regex.Pattern =
//            java.util.regex.Pattern.compile("((#\\d+)|(@\\w+))", java.util.regex.Pattern.MULTILINE)
//    }
//}
