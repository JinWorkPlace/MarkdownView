package com.apps.markdown.sample.samples.editor

import android.text.Editable
import android.text.TextWatcher
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.samples.editor.shared.MarkwonEditTextSample
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher

@MarkwonSampleInfo(
    id = "20200629170348",
    title = "Editor new line continuation",
    description = ("Sample of how new line character can be handled " + "in order to add a _continuation_, for example adding a new " + "bullet list item if current line starts with one"),
    artifacts = [MarkwonArtifact.EDITOR, MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.EDITOR]
)
class EditorNewLineContinuationSample : MarkwonEditTextSample() {
    override fun render() {
        val markwon: Markwon = Markwon.create(context)
        val editor: MarkwonEditor = MarkwonEditor.create(markwon)

        val textWatcher: TextWatcher =
            MarkdownNewLine.wrap(MarkwonEditorTextWatcher.withProcess(editor))

        editText.addTextChangedListener(textWatcher)
    }
}

internal object MarkdownNewLine {
    fun wrap(textWatcher: TextWatcher): TextWatcher {
        return NewLineTextWatcher(textWatcher)
    }

    private class NewLineTextWatcher(
        private val wrapped: TextWatcher
    ) : TextWatcher {
        // NB! matches only bullet lists
        private val RE: java.util.regex.Pattern =
            java.util.regex.Pattern.compile("^( {0,3}[\\-+* ]+)(.+)*$")

        private var selfChange = false

        // this content is pending to be inserted at the beginning
        private var pendingNewLineContent: String? = null
        private var pendingNewLineIndex = 0

        // mark current edited line for removal (range start/end)
        private var clearLineStart = 0
        private var clearLineEnd = 0

        override fun beforeTextChanged(
            s: CharSequence?, start: Int, count: Int, after: Int
        ) {
            // no op
        }

        override fun onTextChanged(
            s: CharSequence, start: Int, before: Int, count: Int
        ) {
            if (selfChange) {
                return
            }

            // just one new character added
            if (before == 0 && count == 1 && '\n' == s[start]) {
                var end = -1
                for (i in start - 1 downTo 0) {
                    if ('\n' == s[i]) {
                        end = i + 1
                        break
                    }
                }

                // start at the very beginning
                if (end < 0) {
                    end = 0
                }

                val pendingNewLineContent: String?

                val clearLineStart: Int
                val clearLineEnd: Int

                val matcher = RE.matcher(s.subSequence(end, start))
                if (matcher.matches()) {
                    // if second group is empty -> remove new line
                    val content = matcher.group(2)
                    if (android.text.TextUtils.isEmpty(content)) {
                        // another empty new line, remove this start
                        clearLineStart = end
                        clearLineEnd = start
                        pendingNewLineContent = null
                    } else {
                        pendingNewLineContent = matcher.group(1)
                        clearLineEnd = 0
                        clearLineStart = 0
                    }
                } else {
                    pendingNewLineContent = null
                    clearLineEnd = 0
                    clearLineStart = 0
                }
                this.pendingNewLineContent = pendingNewLineContent
                this.pendingNewLineIndex = start + 1
                this.clearLineStart = clearLineStart
                this.clearLineEnd = clearLineEnd
            }
        }

        override fun afterTextChanged(s: Editable) {
            if (selfChange) {
                return
            }

            if (pendingNewLineContent != null || clearLineStart < clearLineEnd) {
                selfChange = true
                try {
                    if (pendingNewLineContent != null) {
                        s.insert(pendingNewLineIndex, pendingNewLineContent)
                        pendingNewLineContent = null
                    } else {
                        s.replace(clearLineStart, clearLineEnd, "")
                        clearLineEnd = 0
                        clearLineStart = 0
                    }
                } finally {
                    selfChange = false
                }
            }

            // NB, we assume MarkdownEditor text watcher that only listens for this event,
            // other text-watchers must be interested in other events also
            wrapped.afterTextChanged(s)
        }
    }
}
