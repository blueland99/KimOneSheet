package com.blueland.kimonesheet.view.adapter.holder

import androidx.recyclerview.widget.RecyclerView
import com.blueland.kimonesheet.databinding.ItemMemoBinding
import com.blueland.kimonesheet.db.dao.MappingDto
import com.blueland.kimonesheet.view.adapter.ListAdapter
import java.text.SimpleDateFormat
import java.util.*

class MemoHolder(
    private val binding: ItemMemoBinding, private val listener: ListAdapter.ListListener?
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(pos: Int, item: MappingDto) {
        binding.apply {
            cbBookmark.isChecked = item.bookmark
            tvTitle.text = item.title
            tvContent.text = item.content
            tvDatetime.text = SimpleDateFormat("yyyy-MM-dd hh:mm").format(Date(item.modDate!!))

            cbBookmark.setOnClickListener {
                listener?.itemOnBookmark(pos, item.childId, cbBookmark.isChecked)
            }
            itemView.setOnClickListener {
                listener?.itemOnClick(pos, item)
            }
            itemView.setOnLongClickListener {
                listener?.itemOnLongClick(pos, item)
                return@setOnLongClickListener true
            }
        }
    }
}