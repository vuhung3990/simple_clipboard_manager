package com.tux.simpleclipboadmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.tux.simpleclipboadmanager.db.ClipBoardDao
import com.tux.simpleclipboadmanager.db.Clipboard
import kotlinx.coroutines.experimental.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.factory
import org.kodein.di.generic.instance

class ClipboardService : Service(), ClipboardManager.OnPrimaryClipChangedListener, KodeinAware {
  override val kodein: Kodein by closestKodein()
  private val actionStack1 by instance<String>("actionStack1")
  private val actionStack2 by instance<String>("actionStack2")
  private val actionCopy by instance<String>("actionCopy")
  private val actionStop by instance<String>("actionStop")

  private val notificationId by instance<Int>("notificationId")
  private val clipboardMgr by instance<ClipboardManager>()
  private val notificationManager by instance<NotificationManager>()
  private val clipboardDao by instance<ClipBoardDao>()

  /**
   * create notification chanel for android 26+
   */
  private fun createChanel(notificationManager: NotificationManager) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      val chanelId by instance<String>("chanelId")
      val chanelName by instance<String>("chanelName")
      val channel = createNotificationChannel(chanelId, chanelName)
      notificationManager.createNotificationChannel(channel)
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createNotificationChannel(chanelId: String, chanelName: String): NotificationChannel {
    return NotificationChannel(chanelId, chanelName, NotificationManager.IMPORTANCE_LOW)
      .apply {
        setShowBadge(false)
        lockscreenVisibility = Notification.VISIBILITY_SECRET
        enableVibration(false)
        enableLights(false)
      }
  }

  override fun onCreate() {
    super.onCreate()
    clipboardMgr.addPrimaryClipChangedListener(this)

    launch {
      val lastCopy = clipboardDao.getLast()
      startForeground(notificationId, getNotification(lastCopy?.text))
    }
  }

  private fun getNotification(text: CharSequence?): Notification {
    val bigTextStyle by factory<CharSequence?, NotificationCompat.BigTextStyle>()
    createChanel(notificationManager)
    val notification by factory<NotificationCompat.BigTextStyle, Notification>()
    return notification(bigTextStyle(text))
  }

  /**
   * store last recent copied
   */
  private var previousText: CharSequence? = null

  override fun onPrimaryClipChanged() {
    val text = clipboardMgr.primaryClip?.getItemAt(0)?.text
    // ensure do not repeat
    if (!text.isNullOrBlank() && previousText != text) {
      previousText = text
      notificationManager.notify(notificationId, getNotification(text))

      // save into db
      launch {
        clipboardDao.insert(Clipboard(previousText.toString()))
      }
      Log.w("tag", "onChange: $text")
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    intent?.action?.run {
      when (this) {
        actionStack1 -> {
          Log.w("debug", "STACK1")
        }
        actionStack2 -> {
          Log.w("debug", "STACK2")
        }
        actionCopy -> {
          Log.w("debug", "COPY")
          val intentCopy = Intent(actionCopy).apply {
            putExtra("text", "fuck you guys")
          }
          LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentCopy)
        }
        else -> {
          Log.w("debug", "stop")
          stopForeground(true)
          stopSelf()
        }
      }
    }
    return START_STICKY
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onDestroy() {
    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(actionStop))
    Log.d("debug", "stop")
    clipboardMgr.removePrimaryClipChangedListener(this)
    super.onDestroy()
  }
}
