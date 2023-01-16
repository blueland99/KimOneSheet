package com.blueland.kimonesheet.view.adapter.holder

import androidx.recyclerview.widget.RecyclerView
import com.blueland.kimonesheet.databinding.ItemListBinding
import com.blueland.kimonesheet.model.MemoModel
import com.blueland.kimonesheet.view.adapter.ListAdapter
import java.text.SimpleDateFormat

class ListHolder(
    private val binding: ItemListBinding, private val listener: ListAdapter.ListListener?
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(pos: Int, item: MemoModel) {
        binding.apply {
            tvTitle.text = item.title
            tvContent.text = item.content
            tvDatetime.text = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(item.datetime)

            itemView.setOnClickListener {
                listener?.itemOnClick(pos, item)
            }
        }
    }
}