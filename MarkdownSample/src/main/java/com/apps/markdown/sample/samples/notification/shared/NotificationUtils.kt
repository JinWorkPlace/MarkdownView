package com.apps.markdown.sample.samples.notification.shared

import android.app.NotificationChannel
import android.app.NotificationManager
import android.widget.RemoteViews
import com.apps.markdown.sample.R

object NotificationUtils {
    private const val ID = 2
    private const val CHANNEL_ID = "2"

    fun display(context: android.content.Context, cs: CharSequence) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager ?: return

        ensureChannel(
            manager, CHANNEL_ID
        )

        val builder: android.app.Notification.Builder =
            android.app.Notification.Builder(context).setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(context.getString(R.string.app_name)).setContentText(cs)
                .setStyle(android.app.Notification.BigTextStyle().bigText(cs))

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID)
        }

        manager.notify(
            ID, builder.build()
        )
    }

    fun display(context: android.content.Context, remoteViews: RemoteViews) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager == null) {
            return
        }

        ensureChannel(
            manager, CHANNEL_ID
        )

        val builder: android.app.Notification.Builder =
            android.app.Notification.Builder(context).setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(context.getString(R.string.app_name))

        builder.setCustomContentView(remoteViews).setCustomBigContentView(remoteViews)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID)
        }

        manager.notify(
            ID, builder.build()
        )
    }

    private fun ensureChannel(manager: NotificationManager, channelId: String) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return
        }

        val channel: NotificationChannel? = manager.getNotificationChannel(channelId)
        if (channel == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }
}
