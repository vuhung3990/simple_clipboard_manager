package com.tux.simpleclipboadmanager.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "clipboard")
public class Clipboard {

  @PrimaryKey(autoGenerate = true)
  public int id;

  public String text;

  public Clipboard(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return "Clipboard{" +
        "id=" + id +
        ", text='" + text + '\'' +
        '}';
  }
}
