package com.apps.markdown.sample.utils

import android.text.Editable
import android.text.TextWatcher

abstract class TextWatcherAdapter : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
    }

    interface AfterTextChanged {
        fun afterTextChanged(s: Editable?)
    }

    companion object {
        fun afterTextChanged(afterTextChanged: AfterTextChanged): TextWatcher {
            return object : TextWatcherAdapter() {
                override fun afterTextChanged(s: Editable?) {
                    afterTextChanged.afterTextChanged(s)
                }
            }
        }
    }
}
