package io.noties.markwon

import android.os.Build
import android.text.Spanned
import android.util.Log
import android.widget.TextView
import android.widget.TextView.BufferType
import androidx.core.text.PrecomputedTextCompat
import io.noties.markwon.Markwon.TextSetter
import java.lang.ref.WeakReference
import java.util.concurrent.Executor

/**
 * Please note this class requires `androidx.core:core` artifact being explicitly added to your dependencies.
 * Please do not use with `markwon-recycler` as it will lead to bad item rendering (due to async nature)
 *
 * @see Markwon.TextSetter
 *
 * @since 4.1.0
 */
class PrecomputedTextSetterCompat internal constructor(private val executor: Executor) :
    TextSetter {
    override fun setText(
        textView: TextView, markdown: Spanned, bufferType: BufferType, onComplete: Runnable
    ) {
        val reference = WeakReference(textView)
        executor.execute {
            try {
                val precomputedTextCompat: PrecomputedTextCompat? =
                    precomputedText(reference.get(), markdown)
                if (precomputedTextCompat != null) {
                    applyText(reference.get(), precomputedTextCompat, bufferType, onComplete)
                }
            } catch (t: Throwable) {
                Log.e("PrecomputdTxtSetterCmpt", "Exception during pre-computing text", t)
                // apply initial markdown
                applyText(reference.get(), markdown, bufferType, onComplete)
            }
        }
    }

    companion object {
        /**
         * @param executor for background execution of text pre-computation
         */
        fun create(executor: Executor): PrecomputedTextSetterCompat {
            return PrecomputedTextSetterCompat(executor)
        }

        private fun precomputedText(textView: TextView?, spanned: Spanned): PrecomputedTextCompat? {
            if (textView == null) {
                return null
            }

            val params: PrecomputedTextCompat.Params

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                params = PrecomputedTextCompat.Params(textView.textMetricsParams)
            } else {
                val builder = PrecomputedTextCompat.Params.Builder(textView.paint)
                builder.setBreakStrategy(textView.breakStrategy)
                    .setHyphenationFrequency(textView.hyphenationFrequency)

                params = builder.build()
            }

            return PrecomputedTextCompat.create(spanned, params)
        }

        private fun applyText(
            textView: TextView?, text: Spanned, bufferType: BufferType, onComplete: Runnable
        ) {
            textView?.post {
                textView.setText(text, bufferType)
                onComplete.run()
            }
        }
    }
}
