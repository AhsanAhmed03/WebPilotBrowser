package com.browser.app.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.browser.app.R
import com.browser.app.models.BrowserTab

class TabsAdapter(
    private val tabs: MutableList<BrowserTab>,
    private val currentTabId: String?,
    private val onTabClick: (BrowserTab) -> Unit,
    private val onTabClose: (BrowserTab) -> Unit
) : RecyclerView.Adapter<TabsAdapter.TabViewHolder>() {

    inner class TabViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardTab)
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val ivFavicon: ImageView = view.findViewById(R.id.ivFavicon)
        val tvTitle: TextView = view.findViewById(R.id.tvTabTitle)
        val tvUrl: TextView = view.findViewById(R.id.tvTabUrl)
        val btnClose: ImageButton = view.findViewById(R.id.btnCloseTab)
        val ivActive: View = view.findViewById(R.id.activeIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tab, parent, false)
        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        val tab = tabs[position]
        val isActive = tab.id == currentTabId

        holder.tvTitle.text = tab.title.ifEmpty { "New Tab" }
        holder.tvUrl.text = tab.url?.let {
            if (it.length > 40) it.take(40) + "…" else it
        } ?: ""

        if (tab.thumbnail != null) {
            holder.ivThumbnail.setImageBitmap(tab.thumbnail)
            holder.ivThumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.bg_tab_empty)
        }

        if (tab.favicon != null) {
            holder.ivFavicon.setImageBitmap(tab.favicon)
        } else {
            holder.ivFavicon.setImageResource(R.drawable.ic_globe)
        }

        holder.ivActive.visibility = if (isActive) View.VISIBLE else View.GONE
        holder.cardView.setCardBackgroundColor(
            if (isActive) Color.parseColor("#1E58A6FF") else Color.parseColor("#21262D")
        )

        holder.cardView.setOnClickListener { onTabClick(tab) }
        holder.btnClose.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                tabs.removeAt(pos)
                notifyItemRemoved(pos)
                onTabClose(tab)
            }
        }
    }

    override fun getItemCount() = tabs.size
}
