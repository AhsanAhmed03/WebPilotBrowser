package com.browser.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.browser.app.R
import com.browser.app.models.SuggestionItem
import com.browser.app.models.SuggestionType

class SearchSuggestionAdapter(
    private var items: MutableList<SuggestionItem> = mutableListOf(),
    private val onItemClick: (SuggestionItem) -> Unit
) : RecyclerView.Adapter<SearchSuggestionAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivSuggIcon)
        val tvText: TextView = view.findViewById(R.id.tvSuggText)
        val tvBadge: TextView = view.findViewById(R.id.tvSuggBadge)
        val tvUrl: TextView = view.findViewById(R.id.tvSuggUrl)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvText.text = item.text

        when (item.type) {
            SuggestionType.BOOKMARK -> {
                holder.ivIcon.setImageResource(R.drawable.ic_bookmark_filled)
                holder.ivIcon.setColorFilter(holder.itemView.context.getColor(R.color.accent_blue))
                holder.tvBadge.visibility = View.VISIBLE
                holder.tvBadge.text = "Bookmark"
                holder.tvUrl.visibility = if (item.url != null) View.VISIBLE else View.GONE
                holder.tvUrl.text = item.url
            }
            SuggestionType.HISTORY -> {
                holder.ivIcon.setImageResource(R.drawable.ic_history)
                holder.ivIcon.setColorFilter(holder.itemView.context.getColor(R.color.accent_orange))
                holder.tvBadge.visibility = View.VISIBLE
                holder.tvBadge.text = "History"
                holder.tvUrl.visibility = if (item.url != null) View.VISIBLE else View.GONE
                holder.tvUrl.text = item.url
            }
            SuggestionType.SEARCH -> {
                holder.ivIcon.setImageResource(R.drawable.ic_search)
                holder.ivIcon.setColorFilter(holder.itemView.context.getColor(R.color.text_secondary))
                holder.tvBadge.visibility = View.GONE
                holder.tvUrl.visibility = View.GONE
            }
            SuggestionType.TRENDING -> {
                holder.ivIcon.setImageResource(R.drawable.ic_trending)
                holder.ivIcon.setColorFilter(holder.itemView.context.getColor(R.color.accent_red))
                holder.tvBadge.visibility = View.GONE
                holder.tvUrl.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<SuggestionItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
