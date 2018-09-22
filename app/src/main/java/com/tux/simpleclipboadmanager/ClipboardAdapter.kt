package com.tux.simpleclipboadmanager

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tux.simpleclipboadmanager.db.Clipboard

class ClipboardAdapter(private val layoutInflater: LayoutInflater) :
  RecyclerView.Adapter<ClipboardAdapter.ClipboardViewHolder>() {

  private var itemClickListener: OnItemClickListener? = null
  /**
   * for display list
   */
  private var dataList = mutableListOf<Clipboard>()

  interface OnItemClickListener {
    fun onClickedItem(position: Int)

    fun onLongClickedItem(position: Int)
  }

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
      dataList.addAll(this)
      notifyDataSetChanged()
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipboardViewHolder {
    val view = layoutInflater.inflate(R.layout.item, parent, false)
    return ClipboardViewHolder(view)
  }

  override fun getItemCount(): Int = dataList.size

  override fun onBindViewHolder(holder: ClipboardViewHolder, position: Int) {
    val clipboard = dataList[position]
    holder.bind(clipboard, itemClickListener)
  }

  fun getItemAt(position: Int): Clipboard = dataList[position]

  fun restoreItem(position: Int, item: Clipboard) {
    dataList.add(position, item)
    notifyItemInserted(position)
  }

  fun setItemClickListener(itemClickListener: OnItemClickListener) {
    this.itemClickListener = itemClickListener
  }

  fun insertItemAtTop(clipboard: Clipboard) {
    dataList.add(0, clipboard)
    notifyItemInserted(0)
  }

  fun updateAt(position: Int, clipboard: Clipboard): MutableList<Clipboard> {
    val changedList = mutableListOf<Clipboard>()
    // clear all item with same stack = unset
    val iterator = dataList.listIterator()
    while (iterator.hasNext()) {
      val item = iterator.next()
      if (clipboard != item && clipboard.stack == item.stack) {
        item.stack = Clipboard.STACK_UNSET
        iterator.set(item)
        changedList.add(item)
      }
    }

    dataList[position] = clipboard
    changedList.add(clipboard)
    notifyDataSetChanged()

    return changedList
  }

  inner class ClipboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val text: TextView by lazy { itemView.findViewById(android.R.id.text1) as TextView }
    private val stack: AppCompatImageView by lazy {
      itemView.findViewById(android.R.id.icon1) as AppCompatImageView
    }

    fun bind(
      clipboard: Clipboard, itemClickListener: OnItemClickListener?) {
      text.text = clipboard.text
      itemView.setOnClickListener {
        itemClickListener?.onClickedItem(adapterPosition)
      }
      itemView.setOnLongClickListener {
        itemClickListener?.onLongClickedItem(adapterPosition)
        return@setOnLongClickListener true
      }

      val stackDrawableRes = when (clipboard.stack) {
        Clipboard.STACK_1 -> R.drawable.outline_looks_one_24
        Clipboard.STACK_2 -> R.drawable.outline_looks_two_24
        else -> Clipboard.STACK_UNSET
      }

      stack.setImageResource(stackDrawableRes)
    }
  }
}
