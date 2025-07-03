package io.noties.markwon.editor

import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.widget.EditText
import io.noties.markwon.editor.MarkwonEditor.PreRenderResult
import io.noties.markwon.editor.MarkwonEditor.PreRenderResultListener
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * Implementation of TextWatcher that uses [MarkwonEditor.process] method
 * to apply markdown highlighting right after text changes.
 *
 * @see MarkwonEditor.process
 * @see MarkwonEditor.preRender
 * @see .withProcess
 * @see .withPreRender
 * @since 4.2.0
 */
abstract class MarkwonEditorTextWatcher : TextWatcher {
    abstract override fun afterTextChanged(s: Editable?)

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }


    internal class WithProcess(private val editor: MarkwonEditor) : MarkwonEditorTextWatcher() {
        private var selfChange = false

        override fun afterTextChanged(s: Editable?) {
            if (selfChange) {
                return
            }

            selfChange = true
            try {
                s?.let {
                    editor.process(s)
                }
            } finally {
                selfChange = false
            }
        }
    }

    internal class WithPreRender(
        private val editor: MarkwonEditor,
        private val executorService: ExecutorService,
        editText: EditText
    ) : MarkwonEditorTextWatcher() {
        // As we operate on a single thread (main) we are fine with a regular int
        //  for marking current _generation_
        private var generator = 0

        private var editText: EditText?

        private var future: Future<*>? = null

        private var selfChange = false

        init {
            this.editText = editText
            this.editText!!.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                }

                override fun onViewDetachedFromWindow(v: View) {
                    this@WithPreRender.editText = null
                }
            })
        }

        override fun afterTextChanged(s: Editable?) {
            if (selfChange) {
                return
            }

            // both will be the same here (generator incremented and key assigned incremented value)
            val key = ++this.generator

            if (future != null) {
                future!!.cancel(true)
            }

            // copy current content (it's not good to pass EditText editable to other thread)
            val builder = SpannableStringBuilder(s)

            future = executorService.submit {
                try {
                    editor.preRender(builder, object : PreRenderResultListener {
                        override fun onPreRenderResult(result: PreRenderResult) {
                            val et = editText
                            et?.post {
                                if (key == generator) {
                                    val et = editText
                                    if (et != null) {
                                        selfChange = true
                                        try {
                                            result.dispatchTo(editText!!.text)
                                        } finally {
                                            selfChange = false
                                        }
                                    }
                                }
                            }
                        }
                    })
                } catch (t: Throwable) {
                    val et = editText
                    et?.post { throw RuntimeException(t) }
                }
            }
        }
    }

    companion object {
        fun withProcess(editor: MarkwonEditor): MarkwonEditorTextWatcher {
            return WithProcess(editor)
        }

        fun withPreRender(
            editor: MarkwonEditor,
            executorService: ExecutorService,
            editText: EditText
        ): MarkwonEditorTextWatcher {
            return WithPreRender(editor, executorService, editText)
        }
    }
}
