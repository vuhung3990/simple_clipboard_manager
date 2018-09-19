package com.tux.simpleclipboadmanager.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.support.annotation.WorkerThread

@Dao
interface ClipBoardDao {

  @WorkerThread
  @Insert
  fun insert(clipboard: Clipboard)

  @WorkerThread
  @Query("SELECT * FROM clipboard ORDER BY id DESC LIMIT 1")
  fun getLast(): Clipboard?

  @WorkerThread
  @Query("SELECT * from clipboard")
  fun getAll(): List<Clipboard>?

  @WorkerThread
  @Delete
  fun deleteOne(clipboard: Clipboard)
}