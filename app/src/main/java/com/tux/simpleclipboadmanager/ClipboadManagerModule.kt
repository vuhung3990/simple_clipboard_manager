package com.tux.simpleclipboadmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import org.kodein.di.Kodein
import org.kodein.di.generic.*

class ClipboadManagerModule(val appContext: Context) {
  @RequiresApi(Build.VERSION_CODES.O)
  val scmModule = Kodein.Module("simpleClipboardManagerModule") {
    bind<Context>("appContext") with singleton { this@ClipboadManagerModule.appContext }
    // notification chanel
    bind<NotificationChannel>() with singleton {
      createNotificationChannel(instance("chanelId"), instance("chanelName"))
    }
    constant("chanelId") with "SCM"
    constant("chanelName") with "Simple Clipboard Manager"
    constant("notificationId") with 93
    constant("actionCopy") with "COPY"
    constant("actionStack1") with "STACK1"
    constant("actionStack2") with "STACK2"
    constant("actionStop") with "STOP"

    bind<NotificationCompat.BigTextStyle>() with factory { text: CharSequence? ->
      NotificationCompat.BigTextStyle().bigText(text)
    }

    bind<PendingIntent>("pIntentCopy") with singleton {
      PendingIntent.getService(appContext, 0,
        Intent(appContext, ClipboardService::class.java).apply {
          action = instance("actionCopy")
        }, 0)
    }

    bind<PendingIntent>("pIntentStack1") with singleton {
      PendingIntent.getService(appContext, 0,
        Intent(appContext, ClipboardService::class.java).apply {
          action = instance("actionStack1")
        }, 0)
    }

    bind<PendingIntent>("pIntentStack2") with singleton {
      PendingIntent.getService(appContext, 0,
        Intent(appContext, ClipboardService::class.java).apply {
          action = instance("actionStack2")
        }, 0)
    }

    bind<PendingIntent>("pIntentStop") with singleton {
      PendingIntent.getService(appContext, 0,
        Intent(appContext, ClipboardService::class.java).apply {
          action = instance("actionStop")
        }, 0)
    }

    bind<Notification>() with factory { bigTextStyle: NotificationCompat.BigTextStyle ->
      NotificationCompat.Builder(appContext, instance("chanelId"))
        .setSmallIcon(R.mipmap.ic_launcher)
        .setStyle(bigTextStyle)
        .setContentIntent(instance("pIntentCopy"))
        .setContentTitle(appContext.getString(R.string.notification_title))
        .addAction(R.drawable.outline_looks_one_24, "Stack 1", instance("pIntentStack1"))
        .addAction(R.drawable.outline_looks_two_24, "Stack 2", instance("pIntentStack2"))
        .addAction(R.drawable.outline_visibility_off_24, "Stop Tracking", instance("pIntentStop"))
        .build()
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createNotificationChannel(chanelId: String, chanelName: String): NotificationChannel {
    return NotificationChannel(chanelId, chanelName, NotificationManager.IMPORTANCE_HIGH)
      .apply {
        setShowBadge(false)
        lockscreenVisibility = Notification.VISIBILITY_SECRET
        enableVibration(false)
        enableLights(false)
      }
  }
}