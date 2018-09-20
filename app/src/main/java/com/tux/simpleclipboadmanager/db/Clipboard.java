package com.tux.simpleclipboadmanager.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "clipboard")
public class Clipboard {

  public static final int STACK_UNSET = 0;
  public static final int STACK_1 = 1;
  public static final int STACK_2 = 2;

  @PrimaryKey(autoGenerate = true)
  public int id;

  public int stack = STACK_UNSET;

  public String text;

  public Clipboard(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return "Clipboard{" +
      "stack=" + stack +
      ", text='" + text + '\'' +
      '}';
  }
}
