package com.apps.markdown.util.ex

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.apps.markdown.R

//region Animation & Effects

/**
 * Click animation
 */
fun clickAnimation(mContext: Context?, view: View) {
    if (mContext != null) {
        val myAnim = AnimationUtils.loadAnimation(mContext, R.anim.bounce)
        view.startAnimation(myAnim)
    }
}

/**
 * Click animation cho view
 */
fun View.clickAnimation() {
    try {
        if (!isAttachedToWindow) {
            return
        }
        context ?: return
        clickAnimation(context, this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Click với animation
 */
fun View.clickWithAnimation(action: (View) -> Unit) {
    setOnClickListener {
        clickAnimation()
        action.invoke(this)
    }
}
//endregion

//region View Visibility & State Management

/**
 * Ẩn view (INVISIBLE)
 */
fun View.hide() {
    if (!isHide) {
        this.visibility = View.INVISIBLE
    }
}

/**
 * Hiển thị view (VISIBLE)
 */
fun View.show() {
    if (!isShow) {
        this.visibility = View.VISIBLE
    }
}

/**
 * Ẩn view hoàn toàn (GONE)
 */
fun View.gone() {
    if (!isGone) {
        this.visibility = View.GONE
    }
}

/**
 * Show hoặc Gone view
 */
fun View.showOrGone(isShow: Boolean) {
    if (isShow) {
        show()
    } else {
        gone()
    }
}

/**
 * Show hoặc Hide view
 */
fun View.showOrHide(isShow: Boolean) {
    if (isShow) {
        show()
    } else {
        hide()
    }
}

/**
 * Các extension properties để check visibility
 */
val View.isShow: Boolean
    get() = this.isVisible

val View.isHide: Boolean
    get() = this.isInvisible

val View.isGone: Boolean
    get() = this.visibility == View.GONE

//endregion