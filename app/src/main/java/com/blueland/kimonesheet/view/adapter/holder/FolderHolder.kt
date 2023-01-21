package com.blueland.kimonesheet.view.adapter.holder

import androidx.recyclerview.widget.RecyclerView
import com.blueland.kimonesheet.databinding.ItemFolderBinding
import com.blueland.kimonesheet.db.dao.MappingDto
import com.blueland.kimonesheet.view.adapter.ListAdapter

class FolderHolder(
    private val binding: ItemFolderBinding, private val listener: ListAdapter.ListListener?
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(pos: Int, item: MappingDto) {
        binding.apply {
            tvFolder.text = item.folder
            itemView.setOnClickListener {
                listener?.itemOnClick(item)
            }
            itemView.setOnLongClickListener {
                listener?.itemOnLongClick(item)
                return@setOnLongClickListener true
            }
        }
    }
}