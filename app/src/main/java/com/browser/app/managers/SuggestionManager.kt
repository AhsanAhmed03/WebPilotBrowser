package com.browser.app.managers

import android.content.Context
import android.util.Log
import com.browser.app.models.SuggestionItem
import com.browser.app.models.SuggestionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SuggestionManager(private val context: Context) {

    companion object {
        private const val TAG = "SuggestionManager"
        private const val SUGGEST_URL = "https://suggestqueries.google.com/complete/search?client=firefox&q=%s"
        private const val TIMEOUT_MS = 3000
        private const val MAX_GOOGLE = 5
        private const val MAX_LOCAL = 3
    }

    suspend fun getSuggestions(
        query: String,
        bookmarkManager: BookmarkManager,
        historyManager: HistoryManager
    ): List<SuggestionItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<SuggestionItem>()
        if (query.isBlank()) return@withContext results

        val q = query.trim().lowercase()

        bookmarkManager.getAll()
            .filter { it.title.lowercase().contains(q) || it.url.lowercase().contains(q) }
            .take(MAX_LOCAL)
            .forEach {
                results += SuggestionItem(
                    text = it.title.ifBlank { it.url },
                    type = SuggestionType.BOOKMARK,
                    url = it.url
                )
            }

        val bookmarkUrls = results.map { it.url }.toSet()
        historyManager.getAll()
            .filter {
                it.url !in bookmarkUrls &&
                (it.title.lowercase().contains(q) || it.url.lowercase().contains(q))
            }
            .take(MAX_LOCAL)
            .forEach {
                results += SuggestionItem(
                    text = it.title.ifBlank { it.url },
                    type = SuggestionType.HISTORY,
                    url = it.url
                )
            }

        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL(SUGGEST_URL.format(encoded))
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val json = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                val arr = JSONArray(json)
                if (arr.length() > 1) {
                    val suggestions = arr.getJSONArray(1)
                    val already = results.map { it.text.lowercase() }.toSet()
                    for (i in 0 until minOf(suggestions.length(), MAX_GOOGLE)) {
                        val s = suggestions.getString(i)
                        if (s.lowercase() !in already && s.lowercase() != query.lowercase()) {
                            results += SuggestionItem(text = s, type = SuggestionType.SEARCH)
                        }
                    }
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "Suggestion fetch failed: ${e.message}")
        }

        results
    }
}
