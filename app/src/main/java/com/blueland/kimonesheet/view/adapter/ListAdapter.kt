package com.blueland.kimonesheet.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blueland.kimonesheet.databinding.ItemFolderBinding
import com.blueland.kimonesheet.databinding.ItemMemoBinding
import com.blueland.kimonesheet.db.dao.MappingDto
import com.blueland.kimonesheet.view.adapter.holder.FolderHolder
import com.blueland.kimonesheet.view.adapter.holder.MemoHolder

class ListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<MappingDto> = listOf()
    var listener: ListListener? = null

    interface ListListener {
        fun itemOnBookmark(id: Int, bookmarked: Boolean)
        fun itemOnClick(item: MappingDto)
        fun itemOnLongClick(item: MappingDto)
    }

    fun setListItems(items: List<MappingDto>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> FolderHolder(ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener)
            else -> MemoHolder(ItemMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FolderHolder -> holder.bind(position, items[position])
            is MemoHolder -> holder.bind(position, items[position])
        }
    }

    override fun getItemCount(): Int = items.size
}