package com.browser.app.managers

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import android.util.Xml
import com.browser.app.models.FeedItem
import com.browser.app.models.FeedType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class FeedManager(private val context: Context) {

    companion object {
        private const val TAG = "FeedManager"
        private const val TIMEOUT_MS = 8000
        private const val NEWS_RSS_URL = "https://rss.nytimes.com/services/xml/rss/nyt/World.xml"
        private fun trendingUrl(geo: String) = "https://trends.google.com.pk/trending/rss?geo=$geo"
    }

    suspend fun getTrendingTopics(): List<FeedItem> = withContext(Dispatchers.IO) {
        val geo = getUserRegion()
        try {
            fetch(trendingUrl(geo)) { parseTrending(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Trending fetch failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getNewsItems(): List<FeedItem> = withContext(Dispatchers.IO) {
        try {
            fetch(NEWS_RSS_URL) {
                parseNews(it)
            }
        } catch (e: Exception) {
            Log.w(TAG, "News fetch failed: ${e.message}")
            emptyList()
        }
    }

    private fun getUserRegion(): String {
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val iso = tm.networkCountryIso.uppercase().ifBlank { tm.simCountryIso.uppercase() }
            iso.ifBlank { "PK" }
        } catch (e: Exception) {
            "PK"
        }
    }

    private fun <T> fetch(urlStr: String, parser: (InputStream) -> List<T>): List<T> {
        val url = URL(urlStr)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("User-Agent", "Mozilla/5.0 WebPilotBrowser/1.0")
            setRequestProperty("Accept", "application/rss+xml,application/xml,text/xml")
        }
        return if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            conn.inputStream.use { parser(it) }.also { conn.disconnect() }
        } else {
            conn.disconnect()
            emptyList()
        }
    }

    private fun parseTrending(stream: InputStream): List<FeedItem> {
        val items = mutableListOf<FeedItem>()
        val parser = Xml.newPullParser()
        parser.setInput(stream, null)

        var inItem = false
        var title = ""
        var link = ""
        var traffic = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name ?: ""
            when (eventType) {
                XmlPullParser.START_TAG -> when (name) {
                    "item" -> { inItem = true; title = ""; link = ""; traffic = "" }
                    "title" -> if (inItem) title = parser.nextText().trim()
                    "link" -> if (inItem) link = parser.nextText().trim()
                    "ht:approx_traffic" -> if (inItem) traffic = parser.nextText().trim()
                    "description" -> if (inItem && traffic.isEmpty()) {
                        val desc = parser.nextText().trim()
                        if (desc.contains(",") || desc.contains("+")) traffic = desc
                    }
                }
                XmlPullParser.END_TAG -> if (name == "item" && inItem) {
                    if (title.isNotBlank()) {
                        items += FeedItem(
                            title = title,
                            description = if (traffic.isNotBlank()) "$traffic searches" else "",
                            link = link,
                            type = FeedType.TRENDING
                        )
                    }
                    inItem = false
                }
            }
            eventType = parser.next()
        }
        return items.take(15)
    }

    private fun parseNews(stream: InputStream): List<FeedItem> {
        val items = mutableListOf<FeedItem>()
        val parser = Xml.newPullParser()

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(stream, null)

        var inItem = false
        var title = ""
        var desc = ""
        var link = ""
        var pubDate = ""

        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {

            val name = parser.name ?: ""

            when (eventType) {

                XmlPullParser.START_TAG -> {

                    when (name) {

                        "item" -> {
                            inItem = true
                            title = ""
                            desc = ""
                            link = ""
                            pubDate = ""
                        }

                        "title" -> if (inItem)
                            title = parser.nextText().trim()

                        "link" -> if (inItem) {
                            val text = parser.nextText().trim()
                            if (text.isNotEmpty()) {
                                link = text
                            }
                        }

                        "description" -> if (inItem)
                            desc = stripHtml(parser.nextText().trim())

                        "pubDate" -> if (inItem)
                            pubDate = formatDate(parser.nextText().trim())
                    }
                }

                XmlPullParser.END_TAG -> {

                    if (name == "item" && inItem) {

                        if (title.isNotBlank() && link.isNotBlank()) {

                            items += FeedItem(
                                title = title,
                                description = desc.take(120),
                                link = link,
                                pubDate = pubDate,
                                type = FeedType.NEWS
                            )
                        }

                        inItem = false
                    }
                }
            }

            eventType = parser.next()
        }

        return items.take(20)
    }

    private fun stripHtml(html: String): String =
        html.replace(Regex("<[^>]+>"), "").replace("&amp;", "&")
            .replace("&lt;", "<").replace("&gt;", ">").replace("&nbsp;", " ").trim()

    private fun formatDate(raw: String): String {
        return try {
            val parts = raw.split(" ")
            if (parts.size >= 4) "${parts[1]} ${parts[2]} ${parts[3]}" else raw
        } catch (_: Exception) { raw }
    }
}
