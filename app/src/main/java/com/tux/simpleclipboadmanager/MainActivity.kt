package com.tux.simpleclipboadmanager

import android.content.*
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.tux.simpleclipboadmanager.db.ClipBoardDao
import com.tux.simpleclipboadmanager.db.Clipboard
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), KodeinAware, SearchView.OnQueryTextListener,
  ClipboardAdapter.OnItemClickListener {

  override val kodein: Kodein by closestKodein()

  private var isTracking = true
  private val actionCopy by instance<String>("actionCopy")
  private val actionStop by instance<String>("actionStop")
  private val clipboardAdapter by instance<ClipboardAdapter>()
  private val clipboardDao by instance<ClipBoardDao>()
  private val clipboardMgr by instance<ClipboardManager>()
  private val clipDataLabel by instance<String>("clipDataLabel")

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

  /**
   * copy data when click item
   */
  override fun onClickedItem(position: Int) {
    val clipboard = clipboardAdapter.getItemAt(position)
    clipboardMgr.primaryClip = ClipData.newPlainText(clipDataLabel, clipboard.text)

    Snackbar.make(container, clipDataLabel, Snackbar.LENGTH_LONG).show()
  }

  override fun onResume() {
    super.onResume()
    // update when isTracking = false, see broadcast
    item?.setIcon(
      if (isTracking) R.drawable.outline_visibility_off_24 else R.drawable.outline_visibility_24)

    launch {
      val clipboards = clipboardDao.getLast100()
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

    // set item click
    clipboardAdapter.setItemClickListener(this)

    // setup swipe to delete
    val swipeHandler = object : SwipeToDeleteCallback(this) {
      override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val lastPosition: Int = viewHolder.adapterPosition
        val lastItem: Clipboard = clipboardAdapter.getItemAt(lastPosition)

        // update ui
        clipboardAdapter.removeItemAt(lastPosition)
        // delete in db
        launch {
          clipboardDao.deleteOne(lastItem)
        }

        Snackbar.make(container, R.string.item_deleted, Snackbar.LENGTH_LONG)
          .setAction(R.string.undo) {
            // restore in db
            launch {
              clipboardDao.insert(lastItem)
            }
            // restore view
            clipboardAdapter.restoreItem(lastPosition, lastItem)
          }.show()
      }
    }
    val itemTouchHelper = ItemTouchHelper(swipeHandler)
    itemTouchHelper.attachToRecyclerView(list)
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

  private var item: MenuItem? = null
  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)

    item = menu.findItem(R.id.action_tracking)
    val searchView = menu.findItem(R.id.action_search).actionView as SearchView
    searchView.setOnQueryTextListener(this)
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

  override fun onQueryTextSubmit(query: String?): Boolean {
    return false
  }

  override fun onQueryTextChange(newText: String?): Boolean {
    Log.d("aaaaa", newText)
    return false
  }
}
