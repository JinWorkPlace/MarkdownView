package com.apps.markdown

import android.view.LayoutInflater
import androidx.core.widget.doAfterTextChanged
import com.apps.markdown.base.BaseActivity
import com.apps.markdown.databinding.ActivityMainBinding
import io.noties.markwon.Markwon

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        val markDown = Markwon.create(this)
        binding.edtInput.doAfterTextChanged { editable ->
            val input = editable.toString()
            val result = markDown.toMarkdown(input)
            binding.tvResult.text = result
        }

//        io.noties.markwon.Markwon.create(this).apply {
//            val markdown = """
//                # Hello World
//                This is a **Markdown** example.
//
//                - Item 1
//                - Item 2
//                - Item 3
//
//                [Click here](https://www.example.com) to visit example.com.
//            """.trimIndent()
//            binding.tvResult.text = this.toMarkdown(markdown)
//        }
    }

    override fun initData() {

    }

    override fun initListener() {

    }
}