package com.apps.markdown

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.apps.markdown.adapter.MarkdownAdapter
import com.apps.markdown.base.BaseActivity
import com.apps.markdown.databinding.ActivityMainBinding
import java.io.File

class MainActivity : BaseActivity<ActivityMainBinding>(), MarkdownAdapter.OnItemClickListener {

    private val adapter by lazy { MarkdownAdapter(this) }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        binding.rvMarkdownFiles.layoutManager = LinearLayoutManager(this)
        binding.rvMarkdownFiles.adapter = adapter
    }

    override fun initData() {
        checkAndRequestPermission()
    }

    override fun initListener() {}

    override fun onItemClick(file: File) {
        val intent = Intent(this, MarkdownDetailActivity::class.java)
        intent.putExtra(MarkdownDetailActivity.EXTRA_FILE_PATH, file.absolutePath)
        startActivity(intent)
    }

    private val readStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                loadMarkdownFiles()
            } else {
                Toast.makeText(
                    this, "Cần cấp quyền để hiển thị file Markdown", Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val manageAllFilesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Quay lại từ Settings, kiểm tra lại quyền
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    loadMarkdownFiles()
                } else {
                    Toast.makeText(
                        this,
                        "Cần cấp quyền Manage All Files để hiển thị file Markdown",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Dưới Android 11, không cần kiểm tra lại
                loadMarkdownFiles()
            }
        }

    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadMarkdownFiles()
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = ("package:$packageName").toUri()
                }
                manageAllFilesLauncher.launch(intent)
            }
        } else {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    loadMarkdownFiles()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    readStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

                else -> {
                    readStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }
    //endregion

    private fun loadMarkdownFiles() {
        val files = queryMarkdownFiles()
        adapter.submitList(files)
    }

    private fun queryMarkdownFiles(): List<File> {
        val result = mutableListOf<File>()
        try {
            val root = Environment.getExternalStorageDirectory()
            root?.walkTopDown()?.forEach { file ->
                if (file.isFile && file.extension.equals("md", true)) {
                    result.add(file)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
