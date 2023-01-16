package com.blueland.kimonesheet.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blueland.kimonesheet.databinding.ItemListBinding
import com.blueland.kimonesheet.db.entity.MemoEntity
import com.blueland.kimonesheet.view.adapter.holder.ListHolder

class ListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: List<MemoEntity> = listOf()
    var listener: ListListener? = null

    interface ListListener {
        fun itemOnClick(pos: Int, item: MemoEntity)
        fun itemOnLongClick(pos: Int, item: MemoEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListHolder(ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ListHolder -> holder.bind(position, items[position])
        }
    }

    override fun getItemCount(): Int = items.size
}