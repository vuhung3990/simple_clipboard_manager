package com.tux.simpleclipboadmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.tux.simpleclipboadmanager.db.ClipBoardDao
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), KodeinAware {
  override val kodein: Kodein by closestKodein()

  private var isTracking = true
  private val actionCopy by instance<String>("actionCopy")
  private val actionStop by instance<String>("actionStop")
  private val clipboardAdapter by instance<ClipboardAdapter>()
  private val clipboardDao by instance<ClipBoardDao>()

  private val trackingReceiver by lazy {
    object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        when (action) {
          actionCopy -> isTracking = false
          else -> {
            Log.w("debug", "copied: ${intent.getStringExtra("text")}")
          }
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    // update when isTracking = false, see broadcast
    if (!isTracking) item.setIcon(R.drawable.outline_visibility_24)

    launch {
      val clipboards = clipboardDao.getAll()
      clipboardAdapter.update(clipboards)
    }
  }

  override fun onDestroy() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(trackingReceiver)
    super.onDestroy()
  }

  private val trackingIntentFilter: IntentFilter by lazy {
    IntentFilter().apply {
      addAction(actionCopy)
      addAction(actionStop)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)

    fab.setOnClickListener { _ ->
      addNewClipboard()
    }
    startService(Intent(this, ClipboardService::class.java))
    LocalBroadcastManager.getInstance(this)
        .registerReceiver(trackingReceiver, trackingIntentFilter)

    list.apply {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(this@MainActivity)
      adapter = clipboardAdapter
      addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
    }
  }

  private fun addNewClipboard() {
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

  lateinit var item: MenuItem
  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)

    item = menu.findItem(R.id.action_tracking)
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
      R.drawable.outline_visibility_24
    } else {
      startService(Intent(this, ClipboardService::class.java))
      R.drawable.outline_visibility_off_24
    }
    isTracking = !isTracking

    item.setIcon(trackingIcon)
  }
}
