package io.noties.markwon.core.spans

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF

internal object ObjectsPool {
    // maybe it's premature optimization, but as all the drawing is done in one thread
    // and we apply needed values before actual drawing it's (I assume) safe to reuse some frequently used objects
    // if one of the spans need some really specific handling for Paint object (like colorFilters, masks, etc)
    // it should instantiate own instance of it
    private val RECT = Rect()
    private val RECT_F = RectF()
    private val PAINT = Paint(Paint.ANTI_ALIAS_FLAG)

    @JvmStatic
    fun rect(): Rect {
        return RECT
    }

    fun rectF(): RectF {
        return RECT_F
    }

    @JvmStatic
    fun paint(): Paint {
        return PAINT
    }
}
