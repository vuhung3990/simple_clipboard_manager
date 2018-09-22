package com.tux.simpleclipboadmanager.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "clipboard")
public class Clipboard {

  public static final int STACK_UNSET = 0;
  public static final int STACK_1 = 1;
  public static final int STACK_2 = 2;

  @PrimaryKey
  public long id;

  public int stack = STACK_UNSET;

  public String text;

  public Clipboard(long id, String text) {
    this.id = id;
    this.text = text;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Clipboard clipboard = (Clipboard) o;

    return id == clipboard.id && stack == clipboard.stack && (text != null ? text
      .equals(clipboard.text) : clipboard.text == null);
  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + stack;
    result = 31 * result + (text != null ? text.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Clipboard{" +
      "stack=" + stack +
      ", text='" + text + '\'' +
      '}';
  }
}
