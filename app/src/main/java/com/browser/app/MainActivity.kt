package com.browser.app

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.browser.app.adapters.*
import com.browser.app.databinding.ActivityMainBinding
import com.browser.app.managers.*
import com.browser.app.models.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var tabManager: TabManager
    private lateinit var historyManager: HistoryManager
    private lateinit var bookmarkManager: BookmarkManager
    private lateinit var adBlockManager: AdBlockManager
    private lateinit var suggestionManager: SuggestionManager
    private lateinit var feedManager: FeedManager
    private lateinit var downloadManager: BrowserDownloadManager

    private lateinit var trendingAdapter: TrendingAdapter
    private lateinit var trendingExtraAdapter: TrendingAdapter
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var newsExtraAdapter: NewsAdapter
    private lateinit var suggestionAdapter: SearchSuggestionAdapter

    private var suggestionJob: Job? = null
    private var trendingExpanded = false
    private var newsExpanded = false

    companion object {
        const val HOME_URL = "webpilot://home"
        private const val DEBOUNCE_MS = 300L
        private const val PREVIEW_COUNT = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initManagers()
        setupSuggestionDropdown()
        setupSwipeRefresh()
        setupListeners()
        setupInitialTab()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        when {
            binding.suggestionCard.visibility == View.VISIBLE -> hideSuggestions()
            tabManager.currentTab?.webView?.canGoBack() == true -> tabManager.currentTab?.webView?.goBack()
            tabManager.currentTab?.url != HOME_URL -> loadHomeUrl()
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        tabManager.tabs.forEach { it.webView?.destroy() }
        downloadManager.destroy()
        super.onDestroy()
    }

    private fun initManagers() {
        tabManager = TabManager(this)
        historyManager = HistoryManager(this)
        bookmarkManager = BookmarkManager(this)
        adBlockManager = AdBlockManager(this)
        suggestionManager = SuggestionManager(this)
        feedManager = FeedManager(this)
        downloadManager = BrowserDownloadManager(this)

        android.webkit.CookieManager.getInstance().setAcceptCookie(true)

        downloadManager.setOnDownloadComplete { fileName ->
            Toast.makeText(this, "Downloaded: $fileName", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeColors(
                getColor(R.color.accent_blue),
                getColor(R.color.accent_green),
                getColor(R.color.accent_purple)
            )
            setProgressBackgroundColorSchemeColor(getColor(R.color.bg_secondary))

            setOnRefreshListener {
                val currentTab = tabManager.currentTab
                if (currentTab == null || currentTab.url == HOME_URL) {
                    loadFeeds()
                    isRefreshing = false
                } else {
                    currentTab.webView?.reload()
                }
            }
        }
    }

    private fun updateSwipeRefreshState(isHomePage: Boolean, webView: WebView? = null) {
        if (isHomePage) {
            binding.swipeRefresh.isEnabled = false
        } else {
            binding.swipeRefresh.isEnabled = true
            binding.swipeRefresh.activeWebView = webView
        }
    }

    private fun setupSuggestionDropdown() {
        suggestionAdapter = SearchSuggestionAdapter { item ->
            hideSuggestions()
            hideKeyboard()
            if (item.url != null) navigateToUrl(item.url) else navigateToUrl(item.text)
        }
        binding.rvSuggestions.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = suggestionAdapter
            itemAnimator = null
        }
    }

    private fun showSuggestions() {
        if ((binding.rvSuggestions.adapter?.itemCount ?: 0) > 0)
            binding.suggestionCard.visibility = View.VISIBLE
    }

    private fun hideSuggestions() {
        binding.suggestionCard.visibility = View.GONE
        suggestionAdapter.updateItems(emptyList())
    }

    private fun fetchSuggestions(query: String) {
        suggestionJob?.cancel()
        if (query.isBlank()) {
            hideSuggestions()
            return
        }
        suggestionJob = lifecycleScope.launch {
            delay(DEBOUNCE_MS)
            val results = suggestionManager.getSuggestions(query, bookmarkManager, historyManager)
            if (isActive) {
                suggestionAdapter.updateItems(results)
                if (results.isNotEmpty()) showSuggestions() else hideSuggestions()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            tabManager.currentTab?.webView?.let {
                if (it.canGoBack()) {
                    it.goBack()
                    showWebView()
                }
            }
        }
        binding.btnForward.setOnClickListener {
            tabManager.currentTab?.webView?.let {
                if (it.canGoForward()) {
                    it.goForward()
                    showWebView()
                }
            }
        }
        binding.btnReload.setOnClickListener {
            tabManager.currentTab?.webView?.reload()
        }
        binding.btnClearUrl.setOnClickListener {
            binding.urlBar.setText("")
            binding.urlBar.requestFocus()
            showKeyboard()
        }

        binding.urlBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEARCH) {
                navigateToUrl(binding.urlBar.text.toString())
                hideSuggestions()
                hideKeyboard()
                true
            } else false
        }

        binding.urlBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.urlBar.selectAll()
                binding.btnClearUrl.visibility = View.VISIBLE
            } else {
                binding.btnClearUrl.visibility = View.GONE
                Handler(Looper.getMainLooper()).postDelayed({ hideSuggestions() }, 150)
            }
        }

        binding.urlBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                if (text.isNotBlank() && binding.urlBar.hasFocus()) {
                    fetchSuggestions(text)
                } else {
                    hideSuggestions()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.homePage.homeSearchBar.setOnClickListener { focusUrlBar() }
        binding.homePage.shortcutGoogle.setOnClickListener { navigateToUrl("https://google.com"); showWebView() }
        binding.homePage.shortcutYouTube.setOnClickListener { navigateToUrl("https://youtube.com"); showWebView() }
        binding.homePage.shortcutGitHub.setOnClickListener { navigateToUrl("https://github.com"); showWebView() }
        binding.homePage.shortcutReddit.setOnClickListener { navigateToUrl("https://reddit.com"); showWebView() }
        binding.homePage.shortcutTwitter.setOnClickListener { navigateToUrl("https://x.com"); showWebView() }

        binding.btnHome.setOnClickListener { loadHomeUrl() }
        binding.btnBookmarks.setOnClickListener { showBookmarksDialog() }
        binding.btnHistory.setOnClickListener { showHistoryDialog() }
        binding.btnTabsContainer.setOnClickListener { showTabsBottomSheet() }
        binding.btnSettings.setOnClickListener { showMenuDialog() }
    }

    private fun setupInitialTab() {
        trendingAdapter = TrendingAdapter { item ->
            val url = "https://www.google.com/search?q=${Uri.encode(item.title)}"
            navigateToUrl(url)
            showWebView()
        }
        trendingExtraAdapter = TrendingAdapter { item ->
            val url = "https://www.google.com/search?q=${Uri.encode(item.title)}"
            navigateToUrl(url)
            showWebView()
        }
        newsAdapter = NewsAdapter { item ->
            val url =
                item.link.ifBlank { "https://www.google.com/search?q=${Uri.encode(item.title)}" }
            navigateToUrl(url)
            showWebView()
        }
        newsExtraAdapter = NewsAdapter { item ->
            val url =
                item.link.ifBlank { "https://www.google.com/search?q=${Uri.encode(item.title)}" }
            navigateToUrl(url)
            showWebView()
        }

        binding.homePage.rvTrending.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = trendingAdapter
            isNestedScrollingEnabled = false
        }
        binding.homePage.rvTrendingExtra.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = trendingExtraAdapter
            isNestedScrollingEnabled = false
        }
        binding.homePage.rvNews.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = newsAdapter
            isNestedScrollingEnabled = false
        }
        binding.homePage.rvNewsExtra.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = newsExtraAdapter
            isNestedScrollingEnabled = false
        }

        binding.homePage.tvTrendingToggle.setOnClickListener { toggleTrending() }
        binding.homePage.tvNewsToggle.setOnClickListener { toggleNews() }

        createNewTab(HOME_URL)
    }

    private fun toggleTrending() {
        trendingExpanded = !trendingExpanded
        binding.homePage.rvTrendingExtra.visibility =
            if (trendingExpanded) View.VISIBLE else View.GONE
        binding.homePage.tvTrendingToggle.text =
            if (trendingExpanded) "Show less  ▴" else "Show more  ▾"
    }

    private fun toggleNews() {
        newsExpanded = !newsExpanded
        binding.homePage.rvNewsExtra.visibility = if (newsExpanded) View.VISIBLE else View.GONE
        binding.homePage.tvNewsToggle.text = if (newsExpanded) "Show less  ▴" else "Show more  ▾"
    }

    private fun showHomePage() {
        tabManager.tabs.forEach { it.webView?.visibility = View.GONE }
        binding.homePage.root.visibility = View.VISIBLE
        updateSwipeRefreshState(isHomePage = true)
        binding.urlBar.setText("")
        binding.urlBar.hint = "Search or enter URL"
        updateSecureIcon("")
        updateNavButtons()

        binding.homePage.tvTrendingRegion.text = try {
            val tm =
                getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
            tm.networkCountryIso.uppercase().ifBlank { "PK" }
        } catch (_: Exception) {
            "PK"
        }
    }

    private fun loadFeeds() {
        trendingExpanded = false
        newsExpanded = false

        binding.homePage.tvTrendingLoading.visibility = View.VISIBLE
        binding.homePage.trendingContainer.visibility = View.GONE
        binding.homePage.tvNewsLoading.visibility = View.VISIBLE
        binding.homePage.newsContainer.visibility = View.GONE

        lifecycleScope.launch {
            launch {
                val trending = feedManager.getTrendingTopics()
                withContext(Dispatchers.Main) {
                    binding.homePage.tvTrendingLoading.visibility = View.GONE
                    if (trending.isNotEmpty()) {
                        val preview = trending.take(PREVIEW_COUNT)
                        val extra = trending.drop(PREVIEW_COUNT)
                        trendingAdapter.updateItems(preview)
                        trendingExtraAdapter.updateItems(extra)
                        binding.homePage.rvTrendingExtra.visibility = View.GONE
                        binding.homePage.tvTrendingToggle.visibility =
                            if (extra.isNotEmpty()) View.VISIBLE else View.GONE
                        binding.homePage.tvTrendingToggle.text = "Show more  ▾"
                        binding.homePage.trendingContainer.visibility = View.VISIBLE
                    } else {
                        binding.homePage.tvTrendingLoading.text = "Trending unavailable"
                        binding.homePage.tvTrendingLoading.visibility = View.VISIBLE
                    }
                }
            }
            launch {
                val news = feedManager.getNewsItems()

                withContext(Dispatchers.Main) {
                    binding.homePage.tvNewsLoading.visibility = View.GONE
                    if (news.isNotEmpty()) {
                        val preview = news.take(PREVIEW_COUNT)
                        val extra = news.drop(PREVIEW_COUNT)
                        newsAdapter.updateItems(preview)
                        newsExtraAdapter.updateItems(extra)
                        binding.homePage.rvNewsExtra.visibility = View.GONE
                        binding.homePage.tvNewsToggle.visibility =
                            if (extra.isNotEmpty()) View.VISIBLE else View.GONE
                        binding.homePage.tvNewsToggle.text = "Show more  ▾"
                        binding.homePage.newsContainer.visibility = View.VISIBLE
                    } else {
                        binding.homePage.tvNewsLoading.text = "News unavailable"
                        binding.homePage.tvNewsLoading.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun showWebView() {
        binding.homePage.root.visibility = View.GONE
        val wv = tabManager.currentTab?.webView
        wv?.visibility = View.VISIBLE
        updateSwipeRefreshState(isHomePage = false, webView = wv)
    }

    private fun loadHomeUrl() {
        hideSuggestions()
        tabManager.currentTab?.let { tab ->
            tabManager.updateTabUrl(tab.id, HOME_URL)
            tabManager.updateTabTitle(tab.id, "Home")
        }
        showHomePage()
        loadFeeds()
    }

    private fun focusUrlBar() {
        binding.urlBar.requestFocus()
        showKeyboard()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun createNewTab(
        url: String,
        fromRestored: BrowserTab? = null,
        incognito: Boolean = false
    ): BrowserTab {
        val webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                allowFileAccess = !incognito
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                cacheMode = if (incognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
                saveFormData = !incognito
                userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
            }
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            visibility = View.GONE
        }

        CookieManager.getInstance().apply {
            setAcceptThirdPartyCookies(webView, !incognito)
        }

        webView.setDownloadListener { dlUrl, userAgent, contentDisposition, mimeType, _ ->
            downloadManager.startDownload(dlUrl, userAgent, contentDisposition, mimeType)
            Toast.makeText(this, "Download started…", Toast.LENGTH_SHORT).show()
        }

        val tab = fromRestored ?: BrowserTab(
            id = System.currentTimeMillis().toString(),
            title = if (incognito) "Incognito" else "New Tab",
            url = url,
            isIncognito = incognito
        )

        setupWebViewClients(webView, tab, incognito)
        binding.webViewContainer.addView(webView)

        val browserTab = tab.copy(webView = webView)
        tabManager.addTab(browserTab)
        switchToTab(browserTab)

        if (url != HOME_URL) webView.loadUrl(url)

        updateTabBadge()
        return browserTab
    }

    private fun setupWebViewClients(webView: WebView, tab: BrowserTab, incognito: Boolean = false) {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url =
                    request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)
                return if (adBlockManager.isEnabled && adBlockManager.shouldBlock(url))
                    WebResourceResponse("text/plain", "utf-8", null)
                else
                    super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let {
                    tabManager.updateTabUrl(tab.id, it)
                    if (tabManager.currentTab?.id == tab.id) {
                        binding.urlBar.setText(it)
                        updateSecureIcon(it)
                        updateNavButtons()
                    }
                }
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                url?.let {
                    val title = view?.title ?: "Untitled"
                    tabManager.updateTabTitle(tab.id, title)
                    tabManager.updateTabUrl(tab.id, it)
                    if (tabManager.currentTab?.id == tab.id) {
                        binding.urlBar.setText(it)
                        updateNavButtons()
                        updateSecureIcon(it)
                    }
                    if (!incognito && !it.startsWith("about:") && it != HOME_URL) {
                        historyManager.addEntry(
                            HistoryEntry(
                                url = it,
                                title = title,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    captureThumbnail(view, tab.id)
                }, 600)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return if (url.startsWith("http") || url.startsWith("https")) false
                else try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); true
                } catch (e: Exception) {
                    false
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (tabManager.currentTab?.id == tab.id) {
                    binding.progressBar.progress = newProgress
                    if (newProgress == 100) {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                title?.let { tabManager.updateTabTitle(tab.id, it) }
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                icon?.let { tabManager.updateTabFavicon(tab.id, it) }
            }
        }
    }

    private fun captureThumbnail(webView: WebView?, tabId: String) {
        webView ?: return
        try {
            val w = 300;
            val h = 200
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
            val canvas = Canvas(bmp)
            canvas.scale(
                w.toFloat() / webView.width.coerceAtLeast(1),
                w.toFloat() / webView.width.coerceAtLeast(1)
            )
            webView.draw(canvas)
            tabManager.updateTabThumbnail(tabId, bmp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun switchToTab(tab: BrowserTab) {
        tabManager.tabs.forEach { it.webView?.visibility = View.GONE }
        binding.homePage.root.visibility = View.GONE
        tabManager.setCurrentTab(tab)

        if (tab.url == HOME_URL || tab.url.isNullOrEmpty()) {
            showHomePage()
            loadFeeds()
        } else {
            val wv = tab.webView
            wv?.visibility = View.VISIBLE
            updateSwipeRefreshState(isHomePage = false, webView = wv)
            binding.urlBar.setText(tab.url)
            updateNavButtons()
            tab.url?.let { updateSecureIcon(it) }
        }
    }

    fun closeTab(tab: BrowserTab) {
        if (tab.isIncognito) {
            tab.webView?.let {
                android.webkit.CookieManager.getInstance().removeAllCookies(null)
                it.clearCache(true)
                it.clearHistory()
                it.clearFormData()
                WebStorage.getInstance().deleteAllData()
            }
        }
        val wasCurrent = tabManager.currentTab?.id == tab.id
        tabManager.closeTab(tab)
        binding.webViewContainer.removeView(tab.webView)
        tab.webView?.destroy()

        if (tabManager.tabs.isEmpty()) createNewTab(HOME_URL)
        else if (wasCurrent) switchToTab(tabManager.tabs.last())

        updateTabBadge()
    }

    private fun navigateToUrl(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return
        val url = when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://www.google.com/search?q=${Uri.encode(trimmed)}"
        }
        hideSuggestions()
        val current = tabManager.currentTab
        if (current == null) {
            createNewTab(url)
        } else {
            showWebView()
            current.webView?.loadUrl(url)
        }
    }

    private fun updateNavButtons() {
        val wv = tabManager.currentTab?.webView
        binding.btnBack.alpha = if (wv?.canGoBack() == true) 1f else 0.35f
        binding.btnForward.alpha = if (wv?.canGoForward() == true) 1f else 0.35f
    }

    private fun updateSecureIcon(url: String) {
        binding.secureIcon.setImageResource(
            if (url.startsWith("https://")) R.drawable.ic_lock else R.drawable.ic_info
        )
    }

    private fun updateTabBadge() {
        val count = tabManager.tabs.size
        binding.tabCountBadge.text = if (count > 99) "99+" else count.toString()
    }

    private fun showTabsBottomSheet() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetStyle)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_tabs, null)
        dialog.setContentView(view)

        val rvTabs = view.findViewById<RecyclerView>(R.id.rvTabs)
        val btnNewTab = view.findViewById<Button>(R.id.btnNewTab)
        val btnIncognito = view.findViewById<Button>(R.id.btnIncognito)
        val tvTabCount = view.findViewById<TextView>(R.id.tvTabCount)
        val btnRestore = view.findViewById<ImageButton>(R.id.btnRestoreTab)

        fun updateCount() {
            tvTabCount.text =
                "${tabManager.tabs.size} Tab${if (tabManager.tabs.size != 1) "s" else ""}"
        }
        updateCount()

        val adapter = TabsAdapter(
            tabs = tabManager.tabs.toMutableList(),
            currentTabId = tabManager.currentTab?.id,
            onTabClick = { tab -> switchToTab(tab); dialog.dismiss() },
            onTabClose = { tab ->
                closeTab(tab); updateCount()
                if (tabManager.tabs.isEmpty()) dialog.dismiss()
            }
        )
        rvTabs.layoutManager = GridLayoutManager(this, 2)
        rvTabs.adapter = adapter

        btnNewTab.setOnClickListener { createNewTab(HOME_URL); dialog.dismiss() }
        btnIncognito.setOnClickListener {
            createNewTab(HOME_URL, incognito = true); dialog.dismiss()
            Toast.makeText(this, "🕵️ Incognito — history not saved", Toast.LENGTH_SHORT).show()
        }
        btnRestore.setOnClickListener {
            tabManager.restoreLastClosedTab()?.let {
                createNewTab(it.url ?: HOME_URL, it); dialog.dismiss()
                Toast.makeText(this, "Tab restored", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "No tabs to restore", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showMenuDialog() {
        val items = arrayOf(
            "🔖 Bookmark this page",
            "🛡️ Ad Block: ${if (adBlockManager.isEnabled) "ON ✓" else "OFF"}",
            "🍪 Cookie Manager",
            "📤 Share",
            "🔍 Find in Page",
            "💾 Downloads",
            "⚙️ Browser Settings"
        )
        AlertDialog.Builder(this, R.style.DialogStyle)
            .setTitle("Menu")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> bookmarkCurrentPage()
                    1 -> toggleAdBlock()
                    2 -> showCookieManager()
                    3 -> sharePage()
                    4 -> showFindInPage()
                    5 -> showDownloadsDialog()
                    6 -> showSettingsDialog()
                }
            }.show()
    }

    private fun showBookmarksDialog() {
        val dialog = AlertDialog.Builder(this, R.style.DialogStyle)
            .setView(R.layout.dialog_bookmarks_history).create()
        dialog.show()

        val tvTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)!!
        val rvItems = dialog.findViewById<RecyclerView>(R.id.rvItems)!!
        val etSearch = dialog.findViewById<EditText>(R.id.etSearch)!!
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)!!
        val tvEmpty = dialog.findViewById<TextView>(R.id.tvEmpty)!!

        tvTitle.text = "⭐ Bookmarks"
        btnClose.setOnClickListener { dialog.dismiss() }

        val bookmarks = bookmarkManager.getAll().toMutableList()
        tvEmpty.visibility = if (bookmarks.isEmpty()) View.VISIBLE else View.GONE
        tvEmpty.text = "No bookmarks yet.\nBookmark pages from the menu (⋮)."

        val adapter = HistoryBookmarkAdapter(
            items = bookmarks.map { GenericItem(it.title, it.url, "bookmark") }.toMutableList(),
            onItemClick = { item -> navigateToUrl(item.subtitle); dialog.dismiss() },
            onItemDelete = { item, _ -> bookmarkManager.remove(item.subtitle) }
        )
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s.toString().lowercase()
                adapter.updateItems(bookmarks.filter {
                    it.title.lowercase().contains(q) || it.url.lowercase().contains(q)
                }.map { GenericItem(it.title, it.url, "bookmark") }.toMutableList())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun bookmarkCurrentPage() {
        val tab = tabManager.currentTab ?: return
        val url = tab.url?.takeIf { it != HOME_URL } ?: return
        val title = tab.title ?: url
        if (bookmarkManager.isBookmarked(url)) {
            bookmarkManager.remove(url)
            Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show()
        } else {
            bookmarkManager.add(
                Bookmark(
                    title = title,
                    url = url,
                    timestamp = System.currentTimeMillis()
                )
            )
            Toast.makeText(this, "Page bookmarked!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showHistoryDialog() {
        val dialog = AlertDialog.Builder(this, R.style.DialogStyle)
            .setView(R.layout.dialog_bookmarks_history).create()
        dialog.show()

        val tvTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)!!
        val rvItems = dialog.findViewById<RecyclerView>(R.id.rvItems)!!
        val etSearch = dialog.findViewById<EditText>(R.id.etSearch)!!
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)!!
        val tvEmpty = dialog.findViewById<TextView>(R.id.tvEmpty)!!
        val btnClearAll = dialog.findViewById<Button>(R.id.btnAction)!!

        tvTitle.text = "🕐 History"
        btnClose.setOnClickListener { dialog.dismiss() }
        btnClearAll.visibility = View.VISIBLE
        btnClearAll.text = "Clear All"

        val history = historyManager.getAll().toMutableList()
        tvEmpty.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
        tvEmpty.text = "No browsing history yet."

        val adapter = HistoryBookmarkAdapter(
            items = history.map { GenericItem(it.title, it.url, "history") }.toMutableList(),
            onItemClick = { item -> navigateToUrl(item.subtitle); dialog.dismiss() },
            onItemDelete = { item, _ -> historyManager.remove(item.subtitle) }
        )
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = adapter

        btnClearAll.setOnClickListener {
            historyManager.clearAll()
            adapter.updateItems(mutableListOf())
            tvEmpty.visibility = View.VISIBLE
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s.toString().lowercase()
                adapter.updateItems(history.filter {
                    it.title.lowercase().contains(q) || it.url.lowercase().contains(q)
                }.map { GenericItem(it.title, it.url, "history") }.toMutableList())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showDownloadsDialog() {
        val dialog = AlertDialog.Builder(this, R.style.DialogStyle)
            .setView(R.layout.dialog_downloads).create()
        dialog.show()

        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)!!
        val rvList = dialog.findViewById<RecyclerView>(R.id.rvDownloads)!!
        val tvEmpty = dialog.findViewById<TextView>(R.id.tvEmpty)!!

        btnClose.setOnClickListener { dialog.dismiss() }

        val downloads = downloadManager.getDownloads()
        tvEmpty.visibility = if (downloads.isEmpty()) View.VISIBLE else View.GONE

        val adapter = DownloadsAdapter(
            items = downloads.toMutableList(),
            onOpen = { item ->
                val intent = downloadManager.openFile(item)
                if (intent != null) {
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "No app to open this file", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onCancel = { item ->
                downloadManager.cancelDownload(item.id)
                Toast.makeText(this, "Download cancelled", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        )
        rvList.layoutManager = LinearLayoutManager(this)
        rvList.adapter = adapter
    }

    private fun toggleAdBlock() {
        adBlockManager.isEnabled = !adBlockManager.isEnabled
        Toast.makeText(
            this,
            "Ad Block ${if (adBlockManager.isEnabled) "enabled" else "disabled"}",
            Toast.LENGTH_SHORT
        ).show()
        tabManager.currentTab?.webView?.reload()
    }

    private fun showCookieManager() {
        val dialog = AlertDialog.Builder(this, R.style.DialogStyle)
            .setView(R.layout.dialog_cookie_manager).create()
        dialog.show()
        dialog.findViewById<ImageButton>(R.id.btnClose)?.setOnClickListener { dialog.dismiss() }
        val switchCookies = dialog.findViewById<Switch>(R.id.switchCookies)
        val switchThirdParty = dialog.findViewById<Switch>(R.id.switchThirdParty)
        val btnClear = dialog.findViewById<Button>(R.id.btnClearCookies)
        val tvInfo = dialog.findViewById<TextView>(R.id.tvCookieInfo)
        val cm = android.webkit.CookieManager.getInstance()
        switchCookies?.isChecked = cm.acceptCookie()
        switchCookies?.setOnCheckedChangeListener { _, c -> cm.setAcceptCookie(c) }
        switchThirdParty?.setOnCheckedChangeListener { _, c ->
            tabManager.tabs.forEach { tab ->
                tab.webView?.let {
                    cm.setAcceptThirdPartyCookies(
                        it,
                        c
                    )
                }
            }
        }
        val cookies = cm.getCookie(tabManager.currentTab?.url ?: "")
        tvInfo?.text = if (cookies != null)
            "Cookies for current page:\n${cookies.take(300)}${if (cookies.length > 300) "…" else ""}"
        else "No cookies for current page"
        btnClear?.setOnClickListener {
            cm.removeAllCookies(null); cm.flush()
            tvInfo?.text = "All cookies cleared"
            Toast.makeText(this, "Cookies cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sharePage() {
        val tab = tabManager.currentTab ?: return
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, tab.url)
            putExtra(Intent.EXTRA_SUBJECT, tab.title)
        }, "Share page"))
    }

    private fun showFindInPage() {
        val view = layoutInflater.inflate(R.layout.dialog_find_in_page, null)
        val dialog = AlertDialog.Builder(this, R.style.DialogStyle).setView(view).create()
        val etFind = view.findViewById<EditText>(R.id.etFind)
        val btnPrev = view.findViewById<ImageButton>(R.id.btnPrev)
        val btnNext = view.findViewById<ImageButton>(R.id.btnNext)
        val btnClose = view.findViewById<ImageButton>(R.id.btnClose)
        val tvRes = view.findViewById<TextView>(R.id.tvResults)
        btnClose.setOnClickListener { tabManager.currentTab?.webView?.clearMatches(); dialog.dismiss() }
        etFind.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tabManager.currentTab?.webView?.findAllAsync(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        tabManager.currentTab?.webView?.setFindListener { ord, total, _ ->
            tvRes.text = if (total > 0) "${ord + 1}/$total" else "0/0"
        }
        btnNext.setOnClickListener { tabManager.currentTab?.webView?.findNext(true) }
        btnPrev.setOnClickListener { tabManager.currentTab?.webView?.findNext(false) }
        dialog.show()
    }

    private fun showSettingsDialog() {
        val dialog = AlertDialog.Builder(this, R.style.DialogStyle)
            .setView(R.layout.dialog_settings).create()
        dialog.show()
        dialog.findViewById<ImageButton>(R.id.btnClose)?.setOnClickListener { dialog.dismiss() }
        val switchJs = dialog.findViewById<Switch>(R.id.switchJavaScript)
        val switchAdblock = dialog.findViewById<Switch>(R.id.switchAdBlock)
        val switchDom = dialog.findViewById<Switch>(R.id.switchDomStorage)
        val btnClearCache = dialog.findViewById<Button>(R.id.btnClearCache)
        val btnClearHist = dialog.findViewById<Button>(R.id.btnClearHistory)
        val settings = tabManager.currentTab?.webView?.settings
        switchJs?.isChecked = settings?.javaScriptEnabled ?: true
        switchAdblock?.isChecked = adBlockManager.isEnabled
        switchDom?.isChecked = settings?.domStorageEnabled ?: true
        switchJs?.setOnCheckedChangeListener { _, c ->
            tabManager.tabs.forEach {
                it.webView?.settings?.javaScriptEnabled = c
            }
        }
        switchAdblock?.setOnCheckedChangeListener { _, c -> adBlockManager.isEnabled = c }
        switchDom?.setOnCheckedChangeListener { _, c ->
            tabManager.tabs.forEach {
                it.webView?.settings?.domStorageEnabled = c
            }
        }
        btnClearCache?.setOnClickListener {
            tabManager.tabs.forEach { it.webView?.clearCache(true) }
            Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show()
        }
        btnClearHist?.setOnClickListener {
            historyManager.clearAll(); WebStorage.getInstance().deleteAllData()
            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.urlBar.windowToken, 0)
        binding.urlBar.clearFocus()
    }

    private fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.urlBar, InputMethodManager.SHOW_IMPLICIT)
    }
}
