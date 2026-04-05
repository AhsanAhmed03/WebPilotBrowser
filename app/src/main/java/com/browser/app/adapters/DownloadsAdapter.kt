package com.browser.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.browser.app.R
import com.browser.app.managers.BrowserDownloadManager

class DownloadsAdapter(
    private var items: MutableList<BrowserDownloadManager.DownloadItem> = mutableListOf(),
    private val onOpen: (BrowserDownloadManager.DownloadItem) -> Unit,
    private val onCancel: (BrowserDownloadManager.DownloadItem) -> Unit
) : RecyclerView.Adapter<DownloadsAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView     = view.findViewById(R.id.tvDownloadName)
        val tvStatus: TextView   = view.findViewById(R.id.tvDownloadStatus)
        val progress: ProgressBar = view.findViewById(R.id.downloadProgress)
        val btnAction: ImageButton = view.findViewById(R.id.btnDownloadAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvName.text = item.fileName
        holder.tvStatus.text = item.statusLabel

        if (item.isRunning || item.isPending) {
            holder.progress.visibility = View.VISIBLE
            holder.progress.progress = item.progressPercent
            holder.btnAction.setImageResource(R.drawable.ic_close)
            holder.btnAction.setOnClickListener { onCancel(item) }
            holder.itemView.setOnClickListener {}
        } else {
            holder.progress.visibility = View.GONE
            holder.btnAction.setImageResource(R.drawable.ic_arrow_forward)
            holder.btnAction.setOnClickListener { if (item.isComplete) onOpen(item) }
            holder.itemView.setOnClickListener { if (item.isComplete) onOpen(item) }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<BrowserDownloadManager.DownloadItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
