package com.blueland.kimonesheet.view.adapter.holder

import androidx.recyclerview.widget.RecyclerView
import com.blueland.kimonesheet.databinding.ItemListBinding
import com.blueland.kimonesheet.db.entity.MemoEntity
import com.blueland.kimonesheet.view.adapter.ListAdapter
import java.text.SimpleDateFormat
import java.util.*

class ListHolder(
    private val binding: ItemListBinding, private val listener: ListAdapter.ListListener?
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(pos: Int, item: MemoEntity) {
        binding.apply {
            tvTitle.text = item.title
            tvContent.text = item.content
            tvDatetime.text = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date(item.modDate))

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