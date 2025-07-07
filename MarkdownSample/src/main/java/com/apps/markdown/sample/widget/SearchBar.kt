package com.apps.markdown.sample.widget

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.apps.markdown.sample.R
import com.apps.markdown.sample.utils.KeyEventUtils
import com.apps.markdown.sample.utils.KeyboardUtils
import com.apps.markdown.sample.utils.TextWatcherAdapter
import com.apps.markdown.sample.utils.TextWatcherAdapter.AfterTextChanged
import com.apps.markdown.sample.utils.hidden

class SearchBar(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val focus: View
    private val textField: TextField
    private val clear: View
    private val cancel: View

    var onSearchListener: ((String?) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        inflate(context, R.layout.view_search_bar, this)

        focus = findViewById(R.id.focus)
        textField = findViewById(R.id.text_field)
        clear = findViewById(R.id.clear)
        cancel = findViewById(R.id.cancel)

        // listen for text state
        textField.addTextChangedListener(TextWatcherAdapter.afterTextChanged(object :
            AfterTextChanged {
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    textFieldChanged(s)
                }
            }
        }))

        fun looseFocus() {
            KeyboardUtils.hide(textField)
            focus.requestFocus()
        }

        // on back pressed - lose focus and hide keyboard
        textField.onBackPressedListener = {
            // hide keyboard and lose focus
            looseFocus()
        }

        textField.setOnFocusChangeListener { _, hasFocus ->
            cancel.hidden = textField.text!!.isEmpty() && !hasFocus
        }

        textField.setOnEditorActionListener { _, _, event ->
            if (KeyEventUtils.isActionUp(event)) {
                looseFocus()
            }
            return@setOnEditorActionListener true
        }

        clear.setOnClickListener {
            textField.setText("")
            // ensure that we have focus when clear is clicked
            if (!textField.hasFocus()) {
                textField.requestFocus()
                // additionally ensure keyboard is showing
                KeyboardUtils.show(textField)
            }
        }

        cancel.setOnClickListener {
            textField.setText("")
            looseFocus()
        }

        isSaveEnabled = false
        textField.isSaveEnabled = false
    }

    fun search(text: String) {
        textField.setText(text)
    }

    private fun textFieldChanged(text: CharSequence) {
        val isEmpty = text.isEmpty()
        clear.hidden = isEmpty
        cancel.hidden = isEmpty && !textField.hasFocus()

        onSearchListener?.invoke(if (text.isEmpty()) null else text.toString())
    }
}