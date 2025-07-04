package com.apps.markdown.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apps.markdown.databinding.ItemMarkdownBinding
import java.io.File

class MarkdownAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MarkdownAdapter.ViewHolder>() {

    private val items = mutableListOf<File>()

    fun submitList(list: List<File>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(file: File)
    }

    inner class ViewHolder(
        private val binding: ItemMarkdownBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(items[position])
                }
            }
        }

        fun bind(file: File) {
            binding.tvFileName.text = file.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMarkdownBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
