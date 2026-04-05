package com.browser.app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.browser.app.R
import com.browser.app.models.FeedItem

class NewsAdapter(
    private var items: MutableList<FeedItem> = mutableListOf(),
    private val onClick: (FeedItem) -> Unit
) : RecyclerView.Adapter<NewsAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvNewsTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvNewsDesc)
        val tvDate: TextView = view.findViewById(R.id.tvNewsDate)
        val tvSource: TextView = view.findViewById(R.id.tvNewsSource)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        Log.d("newsLogs", "News title: ${item.title}")
        Log.d("newsLogs", "News link: ${item.link}")

        holder.tvTitle.text = item.title
        holder.tvDesc.text = item.description
        holder.tvDate.text = item.pubDate
        holder.tvSource.text = "The New York Times"
        holder.tvDesc.visibility = if (item.description.isNotBlank()) View.VISIBLE else View.GONE
        holder.tvDate.visibility = if (item.pubDate.isNotBlank()) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<FeedItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
