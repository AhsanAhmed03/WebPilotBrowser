package com.browser.app.managers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

class BrowserDownloadManager(private val context: Context) {

    private val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val activeDownloads = mutableMapOf<Long, String>()
    private var onDownloadComplete: ((fileName: String) -> Unit)? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: return
            val fileName = activeDownloads.remove(id) ?: return
            onDownloadComplete?.invoke(fileName)
        }
    }

    init {
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun setOnDownloadComplete(callback: (fileName: String) -> Unit) {
        onDownloadComplete = callback
    }

    fun startDownload(url: String, userAgent: String, contentDisposition: String, mimeType: String): Long {
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            .let { makeUnique(it) }

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle(fileName)
            setDescription("Downloading via WebPilot Browser")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            addRequestHeader("User-Agent", userAgent)
            if (mimeType.isNotBlank()) setMimeType(mimeType)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)
        }

        val id = dm.enqueue(request)
        activeDownloads[id] = fileName
        return id
    }

    fun getDownloads(): List<DownloadItem> {
        val items = mutableListOf<DownloadItem>()
        val query = DownloadManager.Query()
        val cursor: Cursor = dm.query(query) ?: return items

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE)) ?: ""
            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            val totalBytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            val downloadedBytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val localUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)) ?: ""
            val mediaType = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE)) ?: ""

            items += DownloadItem(
                id = id,
                fileName = title,
                status = status,
                totalBytes = totalBytes,
                downloadedBytes = downloadedBytes,
                localUri = localUri,
                mimeType = mediaType
            )
        }
        cursor.close()
        return items.sortedByDescending { it.id }
    }

    fun openFile(item: DownloadItem): Intent? {
        val uri = if (item.localUri.startsWith("file://")) {
            val file = File(Uri.parse(item.localUri).path ?: return null)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } else {
            Uri.parse(item.localUri)
        }

        val mime = item.mimeType.ifBlank {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(item.localUri)
            ) ?: "*/*"
        }

        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun cancelDownload(id: Long) {
        dm.remove(id)
        activeDownloads.remove(id)
    }

    fun destroy() {
        try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
    }

    private fun makeUnique(fileName: String): String {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        var file = File(downloadsDir, fileName)
        if (!file.exists()) return fileName

        val dot = fileName.lastIndexOf('.')
        val name = if (dot >= 0) fileName.substring(0, dot) else fileName
        val ext  = if (dot >= 0) fileName.substring(dot) else ""

        var counter = 1
        while (file.exists()) {
            file = File(downloadsDir, "$name($counter)$ext")
            counter++
        }
        return file.name
    }

    data class DownloadItem(
        val id: Long,
        val fileName: String,
        val status: Int,
        val totalBytes: Long,
        val downloadedBytes: Long,
        val localUri: String,
        val mimeType: String
    ) {
        val isComplete get() = status == DownloadManager.STATUS_SUCCESSFUL
        val isFailed get() = status == DownloadManager.STATUS_FAILED
        val isRunning get() = status == DownloadManager.STATUS_RUNNING
        val isPending get() = status == DownloadManager.STATUS_PENDING
        val progressPercent get() = if (totalBytes > 0) (downloadedBytes * 100 / totalBytes).toInt() else 0
        val statusLabel get() = when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> "Done"
            DownloadManager.STATUS_FAILED     -> "Failed"
            DownloadManager.STATUS_RUNNING    -> "$progressPercent%"
            DownloadManager.STATUS_PAUSED     -> "Paused"
            else                              -> "Pending"
        }
    }
}
