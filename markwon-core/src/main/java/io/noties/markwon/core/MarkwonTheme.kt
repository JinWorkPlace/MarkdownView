package io.noties.markwon.core

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.annotation.Px
import androidx.annotation.Size
import io.noties.markwon.utils.ColorUtils.applyAlpha
import io.noties.markwon.utils.Dip
import java.util.Locale
import kotlin.math.min

/**
 * Class to hold *theming* information for rending of markdown.
 *
 *
 * Since version 3.0.0 this class should be considered as *CoreTheme* as its
 * information holds data for core features only. But based on this other components can still use it
 * to display markdown consistently.
 *
 *
 * Since version 3.0.0 this class should not be instantiated manually. Instead a [MarkdownPlugin]
 * should be used: [MarkdownPlugin.configureTheme]
 *
 *
 * Since version 3.0.0 properties related to *strike-through*, *tables* and *HTML*
 * are moved to specific plugins in independent artifacts
 *
 * @see CorePlugin
 *
 * @see io.noties.markwon.MarkwonPlugin.configureTheme
 */
open class MarkwonTheme protected constructor(builder: Builder) {
    protected val linkColor: Int

    // specifies whether we underline links, by default is true
    // @since 4.5.0
    protected val isLinkedUnderlined: Boolean

    // used in quote, lists
    val blockMargin: Int

    // by default it's 1/4th of `blockMargin`
    var blockQuoteWidth: Int
        get() {
            val out: Int = if (blockQuoteWidth == 0) {
                (blockMargin * .25f + .5f).toInt()
            } else {
                blockQuoteWidth
            }
            return out
        }

    // by default it's text color with `BLOCK_QUOTE_DEF_COLOR_ALPHA` applied alpha
    protected val blockQuoteColor: Int

    // by default uses text color (applied for un-ordered lists & ordered (bullets & numbers)
    protected val listItemColor: Int

    // by default the stroke color of a paint object
    protected val bulletListItemStrokeWidth: Int

    // width of bullet, by default min(blockMargin, height) / 2
    protected val bulletWidth: Int

    // by default - main text color
    protected val codeTextColor: Int

    // by default - codeTextColor
    protected val codeBlockTextColor: Int

    // by default 0.1 alpha of textColor/codeTextColor
    protected val codeBackgroundColor: Int

    // by default codeBackgroundColor
    protected val codeBlockBackgroundColor: Int

    // by default `width` of a space char... it's fun and games, but span doesn't have access to paint in `getLeadingMargin`
    // so, we need to set this value explicitly (think of an utility method, that takes TextView/TextPaint and measures space char)
    val codeBlockMargin: Int

    // by default Typeface.MONOSPACE
    protected val codeTypeface: Typeface?

    protected val codeBlockTypeface: Typeface?

    // by default a bit (how much?!) smaller than normal text
    // applied ONLY if default typeface was used, otherwise, not applied
    protected val codeTextSize: Int

    protected val codeBlockTextSize: Int

    // by default paint.getStrokeWidth
    protected val headingBreakHeight: Int

    // by default, text color with `HEADING_DEF_BREAK_COLOR_ALPHA` applied alpha
    protected val headingBreakColor: Int

    // by default, whatever typeface is set on the TextView
    // @since 1.1.0
    protected val headingTypeface: Typeface?

    // by default, we use standard multipliers from the HTML spec (see HEADING_SIZES for values).
    // this library supports 6 heading sizes, so make sure the array you pass here has 6 elements.
    // @since 1.1.0
    protected val headingTextSizeMultipliers: FloatArray?

    // by default textColor with `THEMATIC_BREAK_DEF_ALPHA` applied alpha
    protected val thematicBreakColor: Int

    // by default paint.strokeWidth
    protected val thematicBreakHeight: Int

    init {
        this.linkColor = builder.linkColor
        this.isLinkedUnderlined = builder.isLinkUnderlined
        this.blockMargin = builder.blockMargin
        this.blockQuoteWidth = builder.blockQuoteWidth
        this.blockQuoteColor = builder.blockQuoteColor
        this.listItemColor = builder.listItemColor
        this.bulletListItemStrokeWidth = builder.bulletListItemStrokeWidth
        this.bulletWidth = builder.bulletWidth
        this.codeTextColor = builder.codeTextColor
        this.codeBlockTextColor = builder.codeBlockTextColor
        this.codeBackgroundColor = builder.codeBackgroundColor
        this.codeBlockBackgroundColor = builder.codeBlockBackgroundColor
        this.codeBlockMargin = builder.codeBlockMargin
        this.codeTypeface = builder.codeTypeface
        this.codeBlockTypeface = builder.codeBlockTypeface
        this.codeTextSize = builder.codeTextSize
        this.codeBlockTextSize = builder.codeBlockTextSize
        this.headingBreakHeight = builder.headingBreakHeight
        this.headingBreakColor = builder.headingBreakColor
        this.headingTypeface = builder.headingTypeface
        this.headingTextSizeMultipliers = builder.headingTextSizeMultipliers
        this.thematicBreakColor = builder.thematicBreakColor
        this.thematicBreakHeight = builder.thematicBreakHeight
    }

    /**
     * @since 1.0.5
     */
    fun applyLinkStyle(paint: TextPaint) {
        paint.isUnderlineText = isLinkedUnderlined
        if (linkColor != 0) {
            paint.color = linkColor
        } else {
            // if linkColor is not specified during configuration -> use default one
            paint.color = paint.linkColor
        }
    }

    fun applyLinkStyle(paint: Paint) {
        paint.isUnderlineText = isLinkedUnderlined
        if (linkColor != 0) {
            // by default we will be using text color
            paint.color = linkColor
        } else {
            // @since 1.0.5, if link color is specified during configuration, _try_ to use the
            // default one (if provided paint is an instance of TextPaint)
            if (paint is TextPaint) {
                paint.color = paint.linkColor
            }
        }
    }

    fun applyBlockQuoteStyle(paint: Paint) {
        val color: Int = if (blockQuoteColor == 0) {
            applyAlpha(paint.color, BLOCK_QUOTE_DEF_COLOR_ALPHA)
        } else {
            blockQuoteColor
        }
        paint.style = Paint.Style.FILL
        paint.color = color
    }

    fun applyListItemStyle(paint: Paint) {
        val color: Int = if (listItemColor != 0) {
            listItemColor
        } else {
            paint.color
        }
        paint.color = color

        if (bulletListItemStrokeWidth != 0) {
            paint.strokeWidth = bulletListItemStrokeWidth.toFloat()
        }
    }

    fun getBulletWidth(height: Int): Int {
        val min = min(blockMargin, height) / 2
        val width: Int = if (bulletWidth == 0 || bulletWidth > min) {
            min
        } else {
            bulletWidth
        }

        return width
    }

    /**
     * @since 3.0.0
     */
    fun applyCodeTextStyle(paint: Paint) {
        if (codeTextColor != 0) {
            paint.color = codeTextColor
        }

        if (codeTypeface != null) {
            paint.typeface = codeTypeface

            if (codeTextSize > 0) {
                paint.textSize = codeTextSize.toFloat()
            }
        } else {
            paint.typeface = Typeface.MONOSPACE

            if (codeTextSize > 0) {
                paint.textSize = codeTextSize.toFloat()
            } else {
                paint.textSize = paint.textSize * CODE_DEF_TEXT_SIZE_RATIO
            }
        }
    }

    /**
     * @since 3.0.0
     */
    fun applyCodeBlockTextStyle(paint: Paint) {
        // apply text color, first check for block specific value,
        // then check for code (inline), else do nothing (keep original color of text)

        val textColor = if (codeBlockTextColor != 0) codeBlockTextColor else codeTextColor

        if (textColor != 0) {
            paint.color = textColor
        }

        val typeface = codeBlockTypeface ?: codeTypeface

        if (typeface != null) {
            paint.typeface = typeface

            // please note that we won't be calculating textSize
            // (like we do when no Typeface is provided), if it's some specific typeface
            // we would confuse users about textSize
            val textSize = if (codeBlockTextSize > 0) codeBlockTextSize else codeTextSize

            if (textSize > 0) {
                paint.textSize = textSize.toFloat()
            }
        } else {
            // by default use monospace

            paint.typeface = Typeface.MONOSPACE

            val textSize = if (codeBlockTextSize > 0) codeBlockTextSize else codeTextSize

            if (textSize > 0) {
                paint.textSize = textSize.toFloat()
            } else {
                // calculate default value
                paint.textSize = paint.textSize * CODE_DEF_TEXT_SIZE_RATIO
            }
        }
    }


    /**
     * @since 3.0.0
     */
    fun getCodeBackgroundColor(paint: Paint): Int {
        val color: Int = if (codeBackgroundColor != 0) {
            codeBackgroundColor
        } else {
            applyAlpha(paint.color, CODE_DEF_BACKGROUND_COLOR_ALPHA)
        }
        return color
    }

    /**
     * @since 3.0.0
     */
    fun getCodeBlockBackgroundColor(paint: Paint): Int {
        val color =
            if (codeBlockBackgroundColor != 0) codeBlockBackgroundColor else codeBackgroundColor

        return if (color != 0) color else applyAlpha(
            paint.color, CODE_DEF_BACKGROUND_COLOR_ALPHA
        )
    }

    fun applyHeadingTextStyle(paint: Paint, @IntRange(from = 1, to = 6) level: Int) {
        if (headingTypeface == null) {
            paint.isFakeBoldText = true
        } else {
            paint.typeface = headingTypeface
        }
        val textSizes: FloatArray? = headingTextSizeMultipliers ?: HEADING_SIZES

        if (textSizes != null && textSizes.size >= level) {
            paint.textSize = paint.textSize * textSizes[level - 1]
        } else {
            throw IllegalStateException(
                String.format(
                    Locale.US,
                    "Supplied heading level: %d is invalid, where configured heading sizes are: `%s`",
                    level,
                    textSizes.contentToString()
                )
            )
        }
    }

    fun applyHeadingBreakStyle(paint: Paint) {
        val color: Int = if (headingBreakColor != 0) {
            headingBreakColor
        } else {
            applyAlpha(paint.color, HEADING_DEF_BREAK_COLOR_ALPHA)
        }
        paint.color = color
        paint.style = Paint.Style.FILL
        if (headingBreakHeight >= 0) {
            paint.strokeWidth = headingBreakHeight.toFloat()
        }
    }

    fun applyThematicBreakStyle(paint: Paint) {
        val color: Int = if (thematicBreakColor != 0) {
            thematicBreakColor
        } else {
            applyAlpha(paint.color, THEMATIC_BREAK_DEF_ALPHA)
        }
        paint.color = color
        paint.style = Paint.Style.FILL

        if (thematicBreakHeight >= 0) {
            paint.strokeWidth = thematicBreakHeight.toFloat()
        }
    }

    @Suppress("unused")
    class Builder {
        var linkColor: Int = 0
        var isLinkUnderlined: Boolean = true // @since 4.5.0
        var blockMargin: Int = 0
        var blockQuoteWidth: Int = 0
        var blockQuoteColor: Int = 0
        var listItemColor: Int = 0
        var bulletListItemStrokeWidth: Int = 0
        var bulletWidth: Int = 0
        var codeTextColor: Int = 0
        var codeBlockTextColor: Int = 0 // @since 1.0.5
        var codeBackgroundColor: Int = 0
        var codeBlockBackgroundColor: Int = 0 // @since 1.0.5
        var codeBlockMargin: Int = 0
        var codeTypeface: Typeface? = null
        var codeBlockTypeface: Typeface? = null // @since 3.0.0
        var codeTextSize: Int = 0
        var codeBlockTextSize: Int = 0 // @since 3.0.0
        var headingBreakHeight: Int = -1
        var headingBreakColor: Int = 0
        var headingTypeface: Typeface? = null
        var headingTextSizeMultipliers: FloatArray? = null
        var thematicBreakColor: Int = 0
        var thematicBreakHeight: Int = -1


        internal constructor()

        internal constructor(theme: MarkwonTheme) {
            this.linkColor = theme.linkColor
            this.isLinkUnderlined = theme.isLinkedUnderlined
            this.blockMargin = theme.blockMargin
            this.blockQuoteWidth = theme.blockQuoteWidth
            this.blockQuoteColor = theme.blockQuoteColor
            this.listItemColor = theme.listItemColor
            this.bulletListItemStrokeWidth = theme.bulletListItemStrokeWidth
            this.bulletWidth = theme.bulletWidth
            this.codeTextColor = theme.codeTextColor
            this.codeBlockTextColor = theme.codeBlockTextColor
            this.codeBackgroundColor = theme.codeBackgroundColor
            this.codeBlockBackgroundColor = theme.codeBlockBackgroundColor
            this.codeBlockMargin = theme.codeBlockMargin
            this.codeTypeface = theme.codeTypeface
            this.codeTextSize = theme.codeTextSize
            this.headingBreakHeight = theme.headingBreakHeight
            this.headingBreakColor = theme.headingBreakColor
            this.headingTypeface = theme.headingTypeface
            this.headingTextSizeMultipliers = theme.headingTextSizeMultipliers
            this.thematicBreakColor = theme.thematicBreakColor
            this.thematicBreakHeight = theme.thematicBreakHeight
        }

        fun linkColor(@ColorInt linkColor: Int): Builder {
            this.linkColor = linkColor
            return this
        }

        fun isLinkUnderlined(isLinkUnderlined: Boolean): Builder {
            this.isLinkUnderlined = isLinkUnderlined
            return this
        }

        fun blockMargin(@Px blockMargin: Int): Builder {
            this.blockMargin = blockMargin
            return this
        }

        fun blockQuoteWidth(@Px blockQuoteWidth: Int): Builder {
            this.blockQuoteWidth = blockQuoteWidth
            return this
        }

        fun blockQuoteColor(@ColorInt blockQuoteColor: Int): Builder {
            this.blockQuoteColor = blockQuoteColor
            return this
        }

        fun listItemColor(@ColorInt listItemColor: Int): Builder {
            this.listItemColor = listItemColor
            return this
        }

        fun bulletListItemStrokeWidth(@Px bulletListItemStrokeWidth: Int): Builder {
            this.bulletListItemStrokeWidth = bulletListItemStrokeWidth
            return this
        }

        fun bulletWidth(@Px bulletWidth: Int): Builder {
            this.bulletWidth = bulletWidth
            return this
        }

        fun codeTextColor(@ColorInt codeTextColor: Int): Builder {
            this.codeTextColor = codeTextColor
            return this
        }

        /**
         * @since 1.0.5
         */
        fun codeBlockTextColor(@ColorInt codeBlockTextColor: Int): Builder {
            this.codeBlockTextColor = codeBlockTextColor
            return this
        }

        fun codeBackgroundColor(@ColorInt codeBackgroundColor: Int): Builder {
            this.codeBackgroundColor = codeBackgroundColor
            return this
        }

        /**
         * @since 1.0.5
         */
        fun codeBlockBackgroundColor(@ColorInt codeBlockBackgroundColor: Int): Builder {
            this.codeBlockBackgroundColor = codeBlockBackgroundColor
            return this
        }

        fun codeBlockMargin(@Px codeBlockMargin: Int): Builder {
            this.codeBlockMargin = codeBlockMargin
            return this
        }

        fun codeTypeface(codeTypeface: Typeface): Builder {
            this.codeTypeface = codeTypeface
            return this
        }

        /**
         * @since 3.0.0
         */
        fun codeBlockTypeface(typeface: Typeface): Builder {
            this.codeBlockTypeface = typeface
            return this
        }

        fun codeTextSize(@Px codeTextSize: Int): Builder {
            this.codeTextSize = codeTextSize
            return this
        }

        /**
         * @since 3.0.0
         */
        fun codeBlockTextSize(@Px codeTextSize: Int): Builder {
            this.codeBlockTextSize = codeTextSize
            return this
        }

        fun headingBreakHeight(@Px headingBreakHeight: Int): Builder {
            this.headingBreakHeight = headingBreakHeight
            return this
        }

        fun headingBreakColor(@ColorInt headingBreakColor: Int): Builder {
            this.headingBreakColor = headingBreakColor
            return this
        }

        /**
         * @param headingTypeface Typeface to use for heading elements
         * @return self
         * @since 1.1.0
         */
        fun headingTypeface(headingTypeface: Typeface): Builder {
            this.headingTypeface = headingTypeface
            return this
        }

        /**
         * @param headingTextSizeMultipliers an array of multipliers values for heading elements.
         * The base value for this multipliers is TextView\'s text size
         * @return self
         * @since 1.1.0
         */
        fun headingTextSizeMultipliers(@Size(6) headingTextSizeMultipliers: FloatArray): Builder {
            this.headingTextSizeMultipliers = headingTextSizeMultipliers
            return this
        }

        fun thematicBreakColor(@ColorInt thematicBreakColor: Int): Builder {
            this.thematicBreakColor = thematicBreakColor
            return this
        }

        fun thematicBreakHeight(@Px thematicBreakHeight: Int): Builder {
            this.thematicBreakHeight = thematicBreakHeight
            return this
        }

        fun build(): MarkwonTheme {
            return MarkwonTheme(this)
        }
    }

    companion object {
        /**
         * Factory method to obtain an instance of [MarkwonTheme] with all values as defaults
         *
         * @param context Context in order to resolve defaults
         * @return [MarkwonTheme] instance
         * @see .builderWithDefaults
         * @since 1.0.0
         */
        fun create(context: Context): MarkwonTheme {
            return builderWithDefaults(context).build()
        }

        /**
         * Create an **empty** instance of [Builder] with no default values applied
         *
         *
         * Since version 3.0.0 manual construction of [MarkwonTheme] is not required, instead a
         * [io.noties.markwon.MarkwonPlugin.configureTheme] should be used in order
         * to change certain theme properties
         *
         * @since 3.0.0
         */
        @Suppress("unused")
        fun emptyBuilder(): Builder {
            return Builder()
        }

        /**
         * Factory method to create a [Builder] instance and initialize it with values
         * from supplied [MarkwonTheme]
         *
         * @param copyFrom [MarkwonTheme] to copy values from
         * @return [Builder] instance
         * @see .builderWithDefaults
         * @since 1.0.0
         */
        fun builder(copyFrom: MarkwonTheme): Builder {
            return Builder(copyFrom)
        }

        /**
         * Factory method to obtain a [Builder] instance initialized with default values taken
         * from current application theme.
         *
         * @param context Context to obtain default styling values (colors, etc)
         * @return [Builder] instance
         * @since 1.0.0
         */
        fun builderWithDefaults(context: Context): Builder {
            val dip = Dip.create(context)
            return Builder().codeBlockMargin(dip.toPx(8)).blockMargin(dip.toPx(24))
                .blockQuoteWidth(dip.toPx(4)).bulletListItemStrokeWidth(dip.toPx(1))
                .headingBreakHeight(dip.toPx(1)).thematicBreakHeight(dip.toPx(4))
        }

        protected const val BLOCK_QUOTE_DEF_COLOR_ALPHA: Int = 25

        protected const val CODE_DEF_BACKGROUND_COLOR_ALPHA: Int = 25
        protected const val CODE_DEF_TEXT_SIZE_RATIO: Float = .87f

        protected const val HEADING_DEF_BREAK_COLOR_ALPHA: Int = 75

        // taken from html spec (most browsers render headings like that)
        // is not exposed via protected modifier in order to disallow modification
        private val HEADING_SIZES = floatArrayOf(2f, 1.5f, 1.17f, 1f, .83f, .67f)

        protected const val THEMATIC_BREAK_DEF_ALPHA: Int = 25
    }
}
