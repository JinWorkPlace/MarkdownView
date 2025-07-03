package io.noties.markwon

import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.annotation.VisibleForTesting
import java.util.ArrayDeque
import java.util.Collections
import java.util.Deque
import kotlin.math.max
import kotlin.math.min

/**
 * This class is used to _revert_ order of applied spans. Original SpannableStringBuilder
 * is using an array to store all the information about spans. So, a span that is added first
 * will be drawn first, which leads to subtle bugs (spans receive wrong `x` values when
 * requested to draw itself)
 *
 *
 * since 2.0.0 implements Appendable and CharSequence
 *
 * @since 1.0.1
 */
@Suppress("unused")
class SpannableBuilder @JvmOverloads constructor(
    cs: CharSequence = ""
) : Appendable, CharSequence {
    private val builder: StringBuilder = StringBuilder(cs)

    // actually we might be just using ArrayList
    private val spans: Deque<Span> = ArrayDeque<Span>(8)

    init {
        copySpans(0, cs)
    }

    /**
     * Additional method that takes a String, which is proven to NOT contain any spans
     *
     * @param text String to append
     * @return this instance
     */
    fun append(text: String): SpannableBuilder {
        builder.append(text)
        return this
    }

    override fun append(c: Char): SpannableBuilder {
        builder.append(c)
        return this
    }

    override fun append(cs: CharSequence?): SpannableBuilder {
        copySpans(length, cs)

        builder.append(cs)

        return this
    }

    /**
     * @since 2.0.0 to follow Appendable interface
     */
    override fun append(csq: CharSequence?, start: Int, end: Int): SpannableBuilder {
        val cs = csq?.subSequence(start, end)
        copySpans(length, cs)

        builder.append(cs)

        return this
    }

    fun append(cs: CharSequence, span: Any): SpannableBuilder {
        val length = length
        append(cs)
        setSpan(span, length)
        return this
    }

    fun append(cs: CharSequence, span: Any, flags: Int): SpannableBuilder {
        val length = length
        append(cs)
        setSpan(span, length, length, flags)
        return this
    }

    fun setSpan(span: Any, start: Int): SpannableBuilder {
        return setSpan(span, start, length)
    }

    fun setSpan(span: Any, start: Int, end: Int): SpannableBuilder {
        return setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    fun setSpan(span: Any, start: Int, end: Int, flags: Int): SpannableBuilder {
        spans.push(Span(span, start, end, flags))
        return this
    }

    override val length: Int
        get() = builder.length


    override fun get(index: Int): Char {
        return builder.get(index)
    }

    /**
     * @since 2.0.0 to follow CharSequence interface
     */
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        val out: CharSequence

        // @since 2.0.1 we copy spans to resulting subSequence
        val spans = getSpans(startIndex, endIndex)
        if (spans.isEmpty()) {
            out = builder.subSequence(startIndex, endIndex)
        } else {
            // we should not be SpannableStringBuilderReversed here

            val builder = SpannableStringBuilder(this.builder.subSequence(startIndex, endIndex))

            val length = builder.length

            var s: Int
            var e: Int

            for (span in spans) {
                // we should limit start/end to resulting subSequence length
                //
                // for example, originally it was 5-7 and range 5-7 requested
                // span should have 0-2
                //
                // if a span was fully including resulting subSequence it's start and
                // end must be within 0..length bounds

                s = max(0, span.start - startIndex)
                e = min(length, s + (span.end - span.start))

                builder.setSpan(span.what, s, e, span.flags)
            }
            out = builder
        }

        return out
    }

    /**
     * This method will return all [Span] spans that *overlap* specified range,
     * so if for example a 1..9 range is specified some spans might have 0..6 or 0..10 start/end ranges.
     * <<<<<<< HEAD:markwon-core/src/main/java/ru/noties/markwon/SpannableBuilder.java
     * NB spans are returned in reversed order (not in order that we store them internally)
     * =======
     * NB spans are returned in reversed order (no in order that we store them internally)
     * >>>>>>> master:markwon/src/main/java/ru/noties/markwon/SpannableBuilder.java
     *
     * @since 2.0.1
     */
    fun getSpans(start: Int, end: Int): MutableList<Span> {
        val length = length

        if (!isPositionValid(length, start, end)) {
            // we might as well throw here
            return mutableListOf()
        }

        // all requested
        if (start == 0 && length == end) {
            // but also copy (do not allow external modification)
            val list: MutableList<Span> = ArrayList(spans)
            list.reverse()
            return Collections.unmodifiableList(list)
        }

        val list: MutableList<Span> = ArrayList(0)

        val iterator = spans.descendingIterator()
        var span: Span

        while (iterator.hasNext()) {
            span = iterator.next()
            // we must execute 2 checks: if overlap with specified range or fully include it
            // if span.start is >= range.start -> check if it's before range.end
            // if span.end is <= end -> check if it's after range.start
            if ((span.start >= start && span.start < end) || (span.end <= end && span.end > start) || (span.start < start && span.end > end)) {
                list.add(span)
            }
        }

        return Collections.unmodifiableList(list)
    }

    fun lastChar(): Char {
        return builder.get(length - 1)
    }

    fun removeFromEnd(start: Int): CharSequence {
        // this method is not intended to be used by clients
        // it's a workaround to support tables

        val end = length

        // as we do not expose builder and do not apply spans to it, we are safe to NOT convert to String
        val impl = SpannableStringBuilderReversed(builder.subSequence(start, end))

        val iterator = spans.iterator()

        while (iterator.hasNext()) {
            val span = iterator.next() ?: continue
            if (span.start >= start && span.end <= end) {
                impl.setSpan(
                    span.what,
                    span.start - start,
                    span.end - start,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                iterator.remove()
            }
        }

        builder.replace(start, end, "")

        return impl
    }

    override fun toString(): String {
        return builder.toString()
    }

    fun text(): CharSequence {
        // @since 2.0.0 redirects this call to `#spannableStringBuilder()`
        return spannableStringBuilder()
    }

    /**
     * Simple method to create a SpannableStringBuilder, which is created anyway. Unlike [.text]
     * method which returns the same SpannableStringBuilder there is no need to cast the resulting
     * CharSequence and makes the thing more explicit
     *
     * @since 2.0.0
     */
    fun spannableStringBuilder(): SpannableStringBuilder {
        // okay, in order to not allow external modification and keep our spans order
        // we should not return our builder
        //
        // plus, if this method was called -> all spans would be applied, which potentially
        // breaks the order that we intend to use
        // so, we will defensively copy builder

        // as we do not expose builder and do no apply spans to it, we are safe to NOT to convert to String

        val reversed = SpannableStringBuilderReversed(builder)

        // NB, as e are using Deque -> iteration will be started with last element
        // so, spans will be appearing in the for loop in reverse order
        for (span in spans) {
            reversed.setSpan(span.what, span.start, span.end, span.flags)
        }

        return reversed
    }

    /**
     * @since 3.0.0
     */
    fun clear() {
        builder.setLength(0)
        spans.clear()
    }

    private fun copySpans(index: Int, cs: CharSequence?) {
        // we must identify already reversed Spanned...
        // and (!) iterate backwards when adding (to preserve order)

        if (cs is Spanned) {
            val reversed = cs is SpannableStringBuilderReversed

            val spans = cs.getSpans(0, cs.length, Any::class.java)
            val length = spans?.size ?: 0

            if (length > 0) {
                if (reversed) {
                    var o: Any
                    for (i in length - 1 downTo 0) {
                        o = spans!![i]
                        setSpan(
                            o,
                            index + cs.getSpanStart(o),
                            index + cs.getSpanEnd(o),
                            cs.getSpanFlags(o)
                        )
                    }
                } else {
                    var o: Any
                    for (i in 0..<length) {
                        o = spans!![i]
                        setSpan(
                            o,
                            index + cs.getSpanStart(o),
                            index + cs.getSpanEnd(o),
                            cs.getSpanFlags(o)
                        )
                    }
                }
            }
        }
    }

    /**
     * @since 2.0.1 made public in order to be returned from `getSpans` method, initially added in 1.0.1
     */
    class Span internal constructor(val what: Any, var start: Int, var end: Int, val flags: Int)

    /**
     * @since 2.0.1 made inner class of [SpannableBuilder], initially added in 1.0.1
     */
    internal class SpannableStringBuilderReversed(text: CharSequence?) :
        SpannableStringBuilder(text)

    companion object {
        /**
         * @since 2.0.0
         */
        fun setSpans(builder: SpannableBuilder, spans: Any?, start: Int, end: Int) {
            if (spans != null) {
                // setting a span for an invalid position can lead to silent fail (no exception,
                // but execution is stopped)

                if (!isPositionValid(builder.length, start, end)) {
                    return
                }

                // @since 3.0.1 we introduce another method that recursively applies spans
                // allowing array of arrays (and more)
                setSpansInternal(builder, spans, start, end)
            }
        }

        // @since 2.0.1 package-private visibility for testing
        @VisibleForTesting
        fun isPositionValid(length: Int, start: Int, end: Int): Boolean {
            return end > start && start >= 0 && end <= length
        }


        /**
         * @since 3.0.1
         */
        private fun setSpansInternal(builder: SpannableBuilder, spans: Any?, start: Int, end: Int) {
            if (spans != null) {
                if (spans.javaClass.isArray) {
                    for (o in (spans as Array<*>)) {
                        // @since 3.0.1 recursively apply spans (allow array of arrays)
                        setSpansInternal(builder, o, start, end)
                    }
                } else {
                    builder.setSpan(spans, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }
}
