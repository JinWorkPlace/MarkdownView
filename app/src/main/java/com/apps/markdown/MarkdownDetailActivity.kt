package com.apps.markdown

import android.view.LayoutInflater
import com.apps.markdown.base.BaseActivity
import com.apps.markdown.databinding.ActivityMarkdownDetailBinding
import io.noties.markwon.Markwon
import java.io.File

class MarkdownDetailActivity : BaseActivity<ActivityMarkdownDetailBinding>() {

    companion object {
        const val EXTRA_FILE_PATH = "extra_file_path"
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityMarkdownDetailBinding {
        return ActivityMarkdownDetailBinding.inflate(inflater)
    }

    override fun initView() {
        val path = intent.getStringExtra(EXTRA_FILE_PATH)
        if (path.isNullOrEmpty()) return

        val file = File(path)
        if (!file.exists()) return

        val content = try {
            file.readText()
        } catch (e: Exception) {
            e.printStackTrace()
            "Không thể đọc file"
        }

        Markwon.create(this).setMarkdown(binding.tvContent, content)
    }

    override fun initData() {
    }

    override fun initListener() {
    }
}
