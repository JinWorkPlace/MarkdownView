package com.apps.markdown.sample.widget

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText
import com.apps.markdown.sample.utils.KeyEventUtils

class TextField(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {
    var onBackPressedListener: (() -> Unit)? = null

    override fun onDetachedFromWindow() {
        onBackPressedListener = null
        super.onDetachedFromWindow()
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (isAttachedToWindow) {
            onBackPressedListener?.also { listener ->
                if (hasFocus() && KeyEvent.KEYCODE_BACK == keyCode && KeyEventUtils.isActionUp(event)) {
                    listener()
                }
            }
        }
        return super.onKeyPreIme(keyCode, event)
    }
}