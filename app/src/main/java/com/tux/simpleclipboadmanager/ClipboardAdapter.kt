package com.tux.simpleclipboadmanager

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tux.simpleclipboadmanager.db.Clipboard

class ClipboardAdapter(val layoutInflater: LayoutInflater) :
    RecyclerView.Adapter<ClipboardAdapter.ClipboardViewHolder>() {

  private val dataList = mutableListOf<Clipboard>()

  /**
   * remove item at position
   */
  fun removeItemAt(position: Int) {
    dataList.removeAt(position)
    notifyItemRemoved(position)
  }

  // update data of list
  fun update(data: List<Clipboard>?) {
    data?.run {
      dataList.clear()
      dataList.addAll(data.asReversed())
      notifyDataSetChanged()
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipboardViewHolder {
    val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
    return ClipboardViewHolder(view)
  }

  override fun getItemCount(): Int = dataList.size

  override fun onBindViewHolder(holder: ClipboardViewHolder, position: Int) {
    val clipboard = dataList[position]
    holder.bind(clipboard)
  }

  inner class ClipboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val text: TextView by lazy { itemView.findViewById(android.R.id.text1) as TextView }

    fun bind(clipboard: Clipboard) {
      text.text = clipboard.text
    }
  }
}