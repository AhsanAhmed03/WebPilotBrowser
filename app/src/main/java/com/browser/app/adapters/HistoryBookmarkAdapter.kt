package com.browser.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.browser.app.R
import com.browser.app.models.GenericItem

class HistoryBookmarkAdapter(
    private var items: MutableList<GenericItem>,
    private val onItemClick: (GenericItem) -> Unit,
    private val onItemDelete: (GenericItem, Int) -> Unit
) : RecyclerView.Adapter<HistoryBookmarkAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivItemIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvSubtitle: TextView = view.findViewById(R.id.tvItemSubtitle)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_bookmark, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title.ifEmpty { item.subtitle }
        holder.tvSubtitle.text = item.subtitle
        holder.ivIcon.setImageResource(
            if (item.type == "bookmark") R.drawable.ic_bookmark_filled else R.drawable.ic_history
        )
        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_ID.toInt()) {
                onItemDelete(item, pos)
                items.removeAt(pos)
                notifyItemRemoved(pos)
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: MutableList<GenericItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
