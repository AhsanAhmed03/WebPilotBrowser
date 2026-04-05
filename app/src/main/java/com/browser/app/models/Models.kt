package com.browser.app.models

import android.graphics.Bitmap
import android.webkit.WebView

data class BrowserTab(
    val id: String,
    var title: String = "New Tab",
    var url: String? = null,
    var favicon: Bitmap? = null,
    var thumbnail: Bitmap? = null,
    var webView: WebView? = null,
    val isIncognito: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class HistoryEntry(
    val id: String = System.currentTimeMillis().toString(),
    val url: String,
    val title: String,
    val timestamp: Long
)

data class Bookmark(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val url: String,
    val timestamp: Long
)

data class GenericItem(
    val title: String,
    val subtitle: String,
    val type: String
)
