package com.tux.simpleclipboadmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
  private var isTracking = true
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)

    fab.setOnClickListener { view ->
      addNewClipboad()
    }
//    startService(Intent(this, ClipboardService::class.java))

    showNotification()
  }

  private val notificationManager: NotificationManager? by lazy {
    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  }

  private fun showNotification() {
    createChanel(notificationManager)
    val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(getString(R.string.notification_title))
        .addAction(R.drawable.outline_looks_one_24, "Stack 1", null)
        .addAction(R.drawable.outline_looks_two_24, "Stack 2", null)
        .addAction(R.drawable.outline_visibility_off_24, "ON", null)
        .build()

    notificationManager?.notify(99, notification)
  }

  private val NOTIFICATION_CHANEL_ID: String by lazy { "Simple Clipboard Manager" }
  private val NOTIFICATION_CHANEL_NAME: String by lazy { "Simple Clipboard Manager" }

  /**
   * create notification chanel for android 26+
   */
  private fun createChanel(notificationManager: NotificationManager?) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(NOTIFICATION_CHANEL_ID, NOTIFICATION_CHANEL_NAME,
          NotificationManager.IMPORTANCE_LOW)
          .apply {
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_SECRET
            enableVibration(false)
            enableLights(false)
          }
      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      notificationManager?.createNotificationChannel(channel)
    }
  }

  private fun addNewClipboad() {
    val input = EditText(this).apply {
      setHint(R.string.hint_add_new_clipboard)
      inputType = InputType.TYPE_CLASS_TEXT
    }

    AlertDialog.Builder(this)
        .setTitle(R.string.title_add_new_clipboard)
        .setView(input)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.ok) { _, _ ->

        }
        .show()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    if (item.itemId == R.id.action_tracking) {
      toggleTrackingService(item)
    }
    return true
  }

  private fun toggleTrackingService(item: MenuItem) {
    val trackingIcon = if (isTracking) {
      stopService(Intent(this, ClipboardService::class.java))
      R.drawable.outline_visibility_off_24
    } else {
      startService(Intent(this, ClipboardService::class.java))
      R.drawable.outline_visibility_24
    }
    isTracking = !isTracking

    item.setIcon(trackingIcon)
  }
}
