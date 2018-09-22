package com.tux.simpleclipboadmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.Toast
import com.tux.simpleclipboadmanager.db.ClipBoardDao
import com.tux.simpleclipboadmanager.db.Clipboard
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
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
  private val clipDataLabel by instance<String>("clipDataLabel")
  private val compositeDisposable by instance<CompositeDisposable>()

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

    // get last text from db then show notification
    val lastCopy = clipboardDao.getLast()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        { startForeground(notificationId, getNotification(it.text)) },
        { startForeground(notificationId, getNotification(null)) }
      )
    compositeDisposable.add(lastCopy)
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
      val insertNewClipboard = Maybe.fromCallable {
        clipboardDao.insert(Clipboard(previousText.toString()))
      }
        .subscribeOn(Schedulers.io())
        .subscribe()
      compositeDisposable.add(insertNewClipboard)
      Log.w("tag", "onChange: $text")
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    intent?.action?.run {
      when (this) {
        actionStack1 -> {
          val stack1 = clipboardDao.getStack(Clipboard.STACK_1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
              copyText(it.text)
            }
          compositeDisposable.add(stack1)
        }
        actionStack2 -> {
          Log.w("debug", "STACK2")
          val stack2 = clipboardDao.getStack(Clipboard.STACK_2)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
              copyText(it.text)
            }
          compositeDisposable.add(stack2)
        }
        actionCopy -> {
          Log.w("debug", "COPY")
          val lastRecent = clipboardDao.getLast()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
              { copyText(it.text) },
              {Toast.makeText(this@ClipboardService, R.string.something_went_wrong, Toast.LENGTH_LONG).show()}
            )
          compositeDisposable.add(lastRecent)
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

  private fun copyText(text: String?) {
    text?.run {
      clipboardMgr.primaryClip = ClipData.newPlainText(clipDataLabel, text)
      Toast.makeText(this@ClipboardService, clipDataLabel, Toast.LENGTH_LONG).show()
    }
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onDestroy() {
    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(actionStop))
    compositeDisposable.dispose()
    Log.d("debug", "stop")
    clipboardMgr.removePrimaryClipChangedListener(this)
    super.onDestroy()
  }
}
