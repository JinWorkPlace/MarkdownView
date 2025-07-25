package io.noties.markwon.image

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Looper
import android.os.SystemClock
import android.text.Spanned
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.widget.TextView
import io.noties.markwon.R

object AsyncDrawableScheduler {
    @JvmStatic
    fun schedule(textView: TextView) {
        // we need a simple check if current text has already scheduled drawables
        // we need this in order to allow multiple calls to schedule (different plugins
        // might use AsyncDrawable), but we do not want to repeat the task
        //
        // hm... we need the same thing for unschedule then... we can check if last hash is !null,
        // if it's not -> unschedule, else ignore

        // @since 4.0.0

        val lastTextHashCode =
            textView.getTag(R.id.markwon_drawables_scheduler_last_text_hashcode) as Int?
        val textHashCode = textView.text.hashCode()
        if (lastTextHashCode != null && lastTextHashCode == textHashCode) {
            return
        }
        textView.setTag(R.id.markwon_drawables_scheduler_last_text_hashcode, textHashCode)


        val spans = extractSpans(textView)
        if (spans != null && spans.isNotEmpty()) {
            if (textView.getTag(R.id.markwon_drawables_scheduler) == null) {
                val listener: OnAttachStateChangeListener = object : OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        unschedule(textView)
                        v.removeOnAttachStateChangeListener(this)
                        v.setTag(R.id.markwon_drawables_scheduler, null)
                    }
                }
                textView.addOnAttachStateChangeListener(listener)
                textView.setTag(R.id.markwon_drawables_scheduler, listener)
            }

            // @since 4.1.0
            val invalidator: DrawableCallbackImpl.Invalidator = TextViewInvalidator(textView)

            var drawable: AsyncDrawable?

            for (span in spans) {
                drawable = span.drawable
                drawable.setCallback2(
                    DrawableCallbackImpl(
                        textView, invalidator, drawable.bounds
                    )
                )
            }
        }
    }

    // must be called when text manually changed in TextView
    @JvmStatic
    fun unschedule(view: TextView) {
        // @since 4.0.0

        if (view.getTag(R.id.markwon_drawables_scheduler_last_text_hashcode) == null) {
            return
        }
        view.setTag(R.id.markwon_drawables_scheduler_last_text_hashcode, null)


        val spans = extractSpans(view)
        if (spans != null && spans.isNotEmpty()) {
            for (span in spans) {
                span.drawable.setCallback2(null)
            }
        }
    }

    private fun extractSpans(textView: TextView): Array<AsyncDrawableSpan>? {
        val cs = textView.text
        val length = cs?.length ?: 0

        if (length == 0 || cs !is Spanned) {
            return null
        }

        // we also could've tried the `nextSpanTransition`, but strangely it leads to worse performance
        // than direct getSpans
        return cs.getSpans(0, length, AsyncDrawableSpan::class.java)
    }

    private class DrawableCallbackImpl(
        private val view: TextView, // @since 4.1.0
        private val invalidator: Invalidator, initialBounds: Rect?
    ) : Drawable.Callback {
        // @since 4.1.0
        // interface to be used when bounds change and view must be invalidated
        interface Invalidator {
            fun invalidate()
        }

        private var previousBounds: Rect

        init {
            this.previousBounds = Rect(initialBounds)
        }

        override fun invalidateDrawable(who: Drawable) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                view.post { invalidateDrawable(who) }
                return
            }

            val rect = who.bounds

            // okay... the thing is IF we do not change bounds size, normal invalidate would do
            // but if the size has changed, then we need to update the whole layout...
            if (previousBounds != rect) {
                // @since 4.1.0
                // invalidation moved to upper level (so invalidation can be deferred,
                // and multiple calls combined)
                invalidator.invalidate()
                previousBounds = Rect(rect)
            } else {
                view.postInvalidate()
            }
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
            val delay = `when` - SystemClock.uptimeMillis()
            view.postDelayed(what, delay)
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            view.removeCallbacks(what)
        }
    }

    private class TextViewInvalidator(private val textView: TextView) :
        DrawableCallbackImpl.Invalidator, Runnable {
        override fun invalidate() {
            textView.removeCallbacks(this)
            textView.post(this)
        }

        override fun run() {
            textView.text = textView.text
        }
    }
}
