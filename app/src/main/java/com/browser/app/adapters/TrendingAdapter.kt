package com.browser.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.browser.app.R
import com.browser.app.models.FeedItem

class TrendingAdapter(
    private var items: MutableList<FeedItem> = mutableListOf(),
    private val onClick: (FeedItem) -> Unit
) : RecyclerView.Adapter<TrendingAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumber: TextView = view.findViewById(R.id.tvTrendNumber)
        val tvTopic: TextView = view.findViewById(R.id.tvTrendTopic)
        val tvTraffic: TextView = view.findViewById(R.id.tvTrendTraffic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_trending, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvNumber.text = "${position + 1}"
        holder.tvTopic.text = item.title
        holder.tvTraffic.visibility = if (item.description.isNotBlank()) View.VISIBLE else View.GONE
        holder.tvTraffic.text = item.description
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<FeedItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
