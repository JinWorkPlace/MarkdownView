package com.apps.markdown.sample

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import com.apps.markdown.sample.readme.ReadMeActivity
import com.apps.markdown.sample.sample.SampleManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("unused")
open class App : Application() {

    override fun onCreate() {
        super.onCreate()

        executorService = Executors.newCachedThreadPool()
        sampleManager = SampleManager(this, executorService)

        ensureReadmeShortcut()
    }

    private fun ensureReadmeShortcut() {
        if (Build.VERSION.SDK_INT < 25) {
            return
        }

        val manager = getSystemService(ShortcutManager::class.java) ?: return

        @Suppress("ReplaceNegatedIsEmptyWithIsNotEmpty") if (!manager.dynamicShortcuts.isEmpty()) {
            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            Intent.setComponent = ComponentName(this@App, ReadMeActivity::class.java)
        }

        val shortcut =
            ShortcutInfo.Builder(this, "readme").setShortLabel("README").setIntent(intent).build()
        manager.addDynamicShortcuts(mutableListOf(shortcut))
    }

    companion object {
        lateinit var executorService: ExecutorService

        lateinit var sampleManager: SampleManager
    }
}