package com.sap.codelab.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.sap.codelab.R
import com.sap.codelab.model.Memo
import com.sap.codelab.permission.ReminderPermissions
import com.sap.codelab.view.detail.BUNDLE_MEMO_ID
import com.sap.codelab.view.detail.ViewMemo
import com.sap.codelab.view.home.Home

private const val CHANNEL_ID = "memo_reminders"

/**
 * Shows reminders as status bar notifications. Tapping one opens the memo.
 */
internal class AndroidReminderNotifier(context: Context) : ReminderNotifier {

    private val appContext = context.applicationContext
    private val notificationManager = NotificationManagerCompat.from(appContext)

    init {
        // Creating an existing channel again does nothing, so this is safe to do on every app start.
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(appContext.getString(R.string.reminder_channel_name))
            .setDescription(appContext.getString(R.string.reminder_channel_description))
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission") // Checked through ReminderPermissions
    override fun show(memo: Memo) {
        if (!ReminderPermissions.canPostNotifications(appContext)) {
            return
        }
        val text = ReminderText.of(memo.description)
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(memo.title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openMemoIntent(memo.id))
            .setAutoCancel(true)
            // A reminder for the same memo that is still showing is updated silently
            .setOnlyAlertOnce(true)
            .build()
        notificationManager.notify(memo.id.toNotificationId(), notification)
    }

    override fun cancel(memoId: Long) {
        notificationManager.cancel(memoId.toNotificationId())
    }

    /**
     * Opens the memo, with Home below it so that "back" leads into the app instead of out of it.
     */
    private fun openMemoIntent(memoId: Long): PendingIntent? {
        val viewMemo = Intent(appContext, ViewMemo::class.java).putExtra(BUNDLE_MEMO_ID, memoId)
        return TaskStackBuilder.create(appContext)
            .addNextIntent(Intent(appContext, Home::class.java))
            .addNextIntent(viewMemo)
            // A request code per memo, otherwise all reminders would share one PendingIntent and open the same memo
            .getPendingIntent(memoId.toNotificationId(), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun Long.toNotificationId(): Int = hashCode()
}
