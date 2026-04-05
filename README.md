# 🌐 WebPilot Browser

> A fully-featured Android browser built with Kotlin, WebView, and XML Views — single activity architecture.

---

## ✨ Features

### Browsing
- Full WebView-based browsing with JavaScript, DOM storage, and zoom support
- Back / Forward navigation with alpha feedback
- HTTPS secure indicator in URL bar
- Page reload and hardware-accelerated rendering
- Custom Chrome-like user agent string

### Tabs
- Unlimited multi-tab browsing
- Chrome-style thumbnail grid in a bottom sheet dialog
- Auto-captured page thumbnails after load
- Restore last closed tab (up to 10 tabs in memory)
- Incognito tabs — history, cache, cookies, and form data are never saved

### Home Page (Native XML)
- Branded splash with app logo and tagline
- Tappable search bar that focuses the URL bar
- 2×2 quick-access shortcut grid (Google, YouTube, GitHub, Reddit)
- **Live Trending Topics** — fetched from Google Trends RSS
- **Live News Feed** — fetched from NYT World RSS, displayed as scrollable cards
- Pull-to-refresh reloads both feeds

### Search & Suggestions
- Debounced search suggestions dropdown (300 ms)
- Sources merged and deduplicated:
  - Bookmark matches (shown first, blue badge)
  - History matches (orange badge)
  - Google Autocomplete (no API key required)
- Clear button appears in URL bar while focused

### Bookmarks & History
- Persistent bookmarks via Gson + SharedPreferences
- Persistent browsing history (up to 500 entries)
- Search filter inside both dialogs
- Per-item swipe-to-delete
- Clear-all history button
- Incognito tabs never write to history

### Privacy & Security
- Domain + pattern-based ad blocker (40+ networks blocked)
- Cookie manager — toggle first-party and third-party cookies per WebView
- Incognito mode — no cache, no form data, no history, cookies cleared on tab close
- Clear cache and history from settings

### UI
- Dark GitHub-inspired color palette
- Bottom navigation bar: `[Bookmarks] [History] [Home] [Tabs] [Settings]`
- Home button styled as a raised circular FAB
- Tab count badge on the Tabs button
- Swipe-to-refresh on web pages (only triggers when page is scrolled to top)
- Find-in-page with match counter and prev/next navigation
- Share page via native Android share sheet
- DataBinding throughout — no `findViewById`

---

## 🗂 Project Structure

```
app/src/main/
├── java/com/browser/app/
│   ├── MainActivity.kt
│   ├── BrowserSwipeRefreshLayout.kt
│   ├── adapters/
│   │   ├── TabsAdapter.kt
│   │   ├── HistoryBookmarkAdapter.kt
│   │   ├── SearchSuggestionAdapter.kt
│   │   ├── TrendingAdapter.kt
│   │   └── NewsAdapter.kt
│   ├── managers/
│   │   ├── TabManager.kt
│   │   ├── HistoryManager.kt
│   │   ├── BookmarkManager.kt
│   │   ├── AdBlockManager.kt
│   │   ├── SuggestionManager.kt
│   │   └── FeedManager.kt
│   └── models/
│       ├── Models.kt
│       ├── FeedItem.kt
│       └── SuggestionItem.kt
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── layout_home_page.xml
    │   ├── bottom_sheet_tabs.xml
    │   ├── item_tab.xml
    │   ├── dialog_bookmarks_history.xml
    │   ├── item_history_bookmark.xml
    │   ├── item_suggestion.xml
    │   ├── item_trending.xml
    │   ├── item_news.xml
    │   ├── dialog_cookie_manager.xml
    │   ├── dialog_settings.xml
    │   └── dialog_find_in_page.xml
    ├── drawable/          (30+ vector icons and shape backgrounds)
    └── values/
        ├── colors.xml
        ├── strings.xml
        └── styles.xml
```

---

## 🚀 Getting Started

### Requirements

| Tool | Version |
|---|---|
| Android Studio | Hedgehog 2023.1.1 or newer |
| Kotlin | 2.1.0 |
| Android Gradle Plugin | 8.7.3 |
| Gradle Wrapper | 8.11.1 |
| Compile SDK | 35 (Android 15) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |
| Java | 17 |

### Setup

1. Clone or download the repository
2. Open the `BrowserApp` folder in Android Studio
3. Let Gradle sync automatically
4. Run on a physical device or emulator (API 26+)

No API keys, no external SDKs beyond Gson and Material Components.

---

## 🔧 Configuration

### Change the default news feed

Open `FeedManager.kt` and update the constant:

```kotlin
private const val NEWS_RSS_URL = "https://rss.nytimes.com/services/xml/rss/nyt/World.xml"
```

Replace with any valid RSS 2.0 feed URL.

### Change the trending region default fallback

In `FeedManager.kt`, the region is auto-detected from the SIM/network. To change the fallback from `PK` to another country:

```kotlin
iso.ifBlank { "US" }  // change "PK" to your preferred ISO country code
```

### Increase history limit

In `HistoryManager.kt`:

```kotlin
private val MAX_ENTRIES = 500  // increase or decrease as needed
```

### Add more ad-block rules

In `AdBlockManager.kt`, add entries to `blockedDomains` (set) or `blockedPatterns` (list):

```kotlin
private val blockedDomains = setOf(
    "example-ad-network.com",
    // ...existing entries
)
```

### Change quick-access shortcuts on the home page

In `layout_home_page.xml`, edit the four `LinearLayout` shortcut cards (`shortcutGoogle`, `shortcutYouTube`, `shortcutGitHub`, `shortcutReddit`) and their corresponding click listeners in `MainActivity.kt`:

```kotlin
binding.homePage.shortcutGoogle.setOnClickListener { navigateToUrl("https://google.com"); showWebView() }
```

---

## 🏗 Architecture Notes

### Tab lifecycle

```
createNewTab(url, incognito)
  → WebView constructed with per-tab settings
  → setAcceptThirdPartyCookies(webView, !incognito)
  → TabManager.addTab(browserTab)
  → switchToTab(browserTab)

closeTab(tab)
  → if incognito: clear cache, cookies, history, WebStorage
  → webViewContainer.removeView(webView)
  → webView.destroy()
  → TabManager.closeTab(tab)  ← saves stripped copy to closed-tab stack
```

### Swipe-to-refresh

`BrowserSwipeRefreshLayout` overrides `canChildScrollUp()` to check `activeWebView.scrollY > 4`. The active WebView reference is updated every time a tab is switched or a page starts loading. This prevents the refresh gesture from firing while the user is scrolling down a page.

### Search suggestions pipeline

```
URL bar text change (debounced 300ms)
  → SuggestionManager.getSuggestions(query, bookmarkManager, historyManager)
      1. Bookmark matches (local, instant)
      2. History matches (local, instant, deduped against bookmarks)
      3. Google Autocomplete (network, suggestqueries.google.com)
  → SearchSuggestionAdapter.updateItems(results)
  → suggestionCard visibility = VISIBLE
```

### Feed loading

```
showHomePage() or loadHomeUrl()
  → loadFeeds()
      ├── launch { feedManager.getTrendingTopics() }  ← Dispatchers.IO
      │     → HTTP GET Google Trends RSS (geo=XX)
      │     → XmlPullParser → List<FeedItem>
      │     → trendingAdapter.updateItems(...)
      └── launch { feedManager.getNewsItems() }       ← Dispatchers.IO
            → HTTP GET NYT World RSS
            → XmlPullParser → List<FeedItem>
            → newsAdapter.updateItems(...)
```

---

## 📦 Dependencies

```groovy
implementation 'androidx.core:core-ktx:1.15.0'
implementation 'androidx.appcompat:appcompat:1.7.0'
implementation 'androidx.activity:activity-ktx:1.9.3'
implementation 'com.google.android.material:material:1.12.0'
implementation 'androidx.constraintlayout:constraintlayout:2.2.0'
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'androidx.cardview:cardview:1.0.0'
implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
implementation 'com.google.code.gson:gson:2.11.0'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0'
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.8.7'
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7'
```

No third-party browser engine, image loader, or networking library is used. All network calls use `HttpURLConnection` directly.

---

## 🗺 Future Updates

- [ ] Custom homepage shortcuts (user-editable)
- [ ] Per-site cookie whitelist/blacklist
- [ ] Reader mode
- [ ] Night mode / force dark on all pages
- [ ] Gesture navigation (swipe left/right for back/forward)
- [ ] Multiple news feed sources (user-configurable)
- [ ] Larger ad-block rule set loaded from bundled assets

---

Feel free to reach out to me with any questions or opportunities at (aahsanaahmed26@gmail.com)
- LinkedIn (https://www.linkedin.com/in/ahsan-ahmed-39544b246/)
- Facebook (https://www.facebook.com/profile.php?id=100083917520174).
- YouTube (https://www.youtube.com/@mobileappdevelopment4343)
- Instagram (https://www.instagram.com/ahsanahmed_03/)

## 📄 License

MIT License — free to use, modify, and distribute.
