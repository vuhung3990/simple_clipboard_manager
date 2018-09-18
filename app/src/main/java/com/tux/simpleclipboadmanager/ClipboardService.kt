package com.tux.simpleclipboadmanager

import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log

class ClipboardService : Service(), ClipboardManager.OnPrimaryClipChangedListener {
    private val clipboardMgr: ClipboardManager? by lazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun onCreate() {
        super.onCreate()
        clipboardMgr?.addPrimaryClipChangedListener(this)
    }

    override fun onPrimaryClipChanged() {
        val text = clipboardMgr?.primaryClip?.getItemAt(0)?.text
        Log.d("tag", "onChange: $text")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        clipboardMgr?.removePrimaryClipChangedListener(this)
        super.onDestroy()
    }
}
