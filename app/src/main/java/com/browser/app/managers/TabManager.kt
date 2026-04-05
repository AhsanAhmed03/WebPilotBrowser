package com.browser.app.managers

import android.content.Context
import android.graphics.Bitmap
import com.browser.app.models.BrowserTab

class TabManager(private val context: Context) {
    val tabs = mutableListOf<BrowserTab>()

    private var _currentTab: BrowserTab? = null
    val currentTab: BrowserTab? get() = _currentTab

    private val closedTabs = mutableListOf<BrowserTab>()

    fun addTab(tab: BrowserTab) {
        tabs.add(tab)
    }

    fun setCurrentTab(tab: BrowserTab) {
        _currentTab = tabs.find { it.id == tab.id }
    }

    fun closeTab(tab: BrowserTab) {
        val stripped = tab.copy(webView = null)
        closedTabs.add(0, stripped)
        if (closedTabs.size > 10) closedTabs.removeAt(closedTabs.size - 1)
        tabs.removeAll { it.id == tab.id }
        if (_currentTab?.id == tab.id) _currentTab = tabs.lastOrNull()
    }

    fun restoreLastClosedTab(): BrowserTab? =
        if (closedTabs.isNotEmpty()) closedTabs.removeAt(0) else null

    fun updateTabTitle(tabId: String, title: String) {
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index >= 0) {
            tabs[index] = tabs[index].copy(title = title)
            if (_currentTab?.id == tabId) _currentTab = tabs[index]
        }
    }

    fun updateTabUrl(tabId: String, url: String) {
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index >= 0) {
            tabs[index] = tabs[index].copy(url = url)
            if (_currentTab?.id == tabId) _currentTab = tabs[index]
        }
    }

    fun updateTabFavicon(tabId: String, favicon: Bitmap) {
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index >= 0) {
            tabs[index] = tabs[index].copy(favicon = favicon)
            if (_currentTab?.id == tabId) _currentTab = tabs[index]
        }
    }

    fun updateTabThumbnail(tabId: String, thumbnail: Bitmap) {
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index >= 0) {
            tabs[index] = tabs[index].copy(thumbnail = thumbnail)
            if (_currentTab?.id == tabId) _currentTab = tabs[index]
        }
    }
}
