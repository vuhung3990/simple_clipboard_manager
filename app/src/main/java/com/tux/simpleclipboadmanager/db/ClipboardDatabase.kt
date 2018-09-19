package com.tux.simpleclipboadmanager.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [Clipboard::class], version = 1, exportSchema = false)
abstract class ClipboardDatabase : RoomDatabase() {

  abstract fun clipBoardDao(): ClipBoardDao
}