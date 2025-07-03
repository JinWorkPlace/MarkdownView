package io.noties.markwon

import android.text.Spanned
import android.widget.TextView
import android.widget.TextView.BufferType
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.PrecomputedTextCompat
import io.noties.markwon.Markwon.TextSetter
import java.util.concurrent.Executor

/**
 * Please note this class requires `androidx.core:core` artifact being explicitly added to your dependencies.
 * This is intended to be used in a RecyclerView.
 *
 * @see Markwon.TextSetter
 *
 * @since 4.3.1
 */
class PrecomputedFutureTextSetterCompat internal constructor(
    private val executor: Executor?
) : TextSetter {
    override fun setText(
        textView: TextView, markdown: Spanned, bufferType: BufferType, onComplete: Runnable
    ) {
        if (textView is AppCompatTextView) {
            val future = PrecomputedTextCompat.getTextFuture(
                markdown, textView.textMetricsParamsCompat, executor
            )
            textView.setTextFuture(future)
            // `setTextFuture` is actually a synchronous call, so we should call onComplete now
            onComplete.run()
        } else {
            throw IllegalStateException("TextView provided is not an instance of AppCompatTextView, cannot call setTextFuture(), textView: $textView")
        }
    }

    companion object {
        /**
         * @param executor for background execution of text pre-computation,
         * if not provided the standard, single threaded one will be used.
         */
        fun create(executor: Executor): PrecomputedFutureTextSetterCompat {
            return PrecomputedFutureTextSetterCompat(executor)
        }

        fun create(): PrecomputedFutureTextSetterCompat {
            return PrecomputedFutureTextSetterCompat(null)
        }
    }
}
