package com.tux.simpleclipboadmanager.db

import android.arch.persistence.room.*
import android.support.annotation.WorkerThread
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface ClipBoardDao {

  @WorkerThread
  @Insert
  fun insert(clipboard: Clipboard)

  @WorkerThread
  @Query("SELECT * FROM clipboard ORDER BY id DESC LIMIT 1")
  fun getLast(): Single<Clipboard>

  @WorkerThread
  @Query("SELECT * FROM clipboard ORDER BY id DESC LIMIT 100")
  fun getLast100(): Maybe<List<Clipboard>>

  @WorkerThread
  @Delete
  fun deleteOne(clipboard: Clipboard)

  @WorkerThread
  @Update
  fun updateOne(clipboard: Clipboard)

  @WorkerThread
  @Update
  fun update(changedList: MutableList<Clipboard>)

  @WorkerThread
  @Query("SELECT * FROM clipboard WHERE stack=:stackNumber LIMIT 1")
  fun getStack(stackNumber: Int): Maybe<Clipboard>
}
