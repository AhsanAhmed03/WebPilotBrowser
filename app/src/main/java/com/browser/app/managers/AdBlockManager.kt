package com.browser.app.managers

import android.content.Context
import android.content.SharedPreferences

class AdBlockManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("adblock_prefs", Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean("adblock_enabled", true)
        set(value) = prefs.edit().putBoolean("adblock_enabled", value).apply()

    private val blockedDomains = setOf(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "ads.google.com", "adservice.google.com", "pagead2.googlesyndication.com",
        "google-analytics.com", "googletagmanager.com", "googletagservices.com",
        "facebook.com/plugins", "connect.facebook.net", "platform.twitter.com",
        "ads.twitter.com", "analytics.twitter.com",
        "amazon-adsystem.com", "assoc-amazon.com",
        "outbrain.com", "taboola.com", "revcontent.com",
        "adnxs.com", "adsrvr.org", "advertising.com",
        "rubiconproject.com", "openx.net", "pubmatic.com",
        "criteo.com", "criteo.net",
        "scorecardresearch.com", "comscore.com",
        "hotjar.com", "mixpanel.com", "segment.com",
        "mouseflow.com", "fullstory.com", "logrocket.com",
        "optimizely.com", "quantserve.com",
        "chartbeat.com", "parsely.com",
        "agkn.com", "bluekai.com", "krxd.net",
        "exelator.com", "demdex.net", "omtrdc.net",
        "2mdn.net", "moatads.com", "doubleverify.com",
        "adsafeprotected.com", "spotxchange.com",
        "casalemedia.com", "lijit.com", "sovrn.com",
        "gravity.com", "undertone.com", "appnexus.com",
        "contextweb.com", "tidaltv.com", "yieldmanager.com"
    )

    private val blockedPatterns = listOf(
        "/ads/", "/ad/", "/advertisement/", "/tracking/",
        "/tracker/", "/pixel/", "/beacon/", "/analytics/",
        "utm_source=", "utm_medium=", "utm_campaign=",
        "/pagead/", "/adsense/", "/adframe/",
        "doubleclick", "googlesyndication", "adservice"
    )

    fun shouldBlock(url: String): Boolean {
        if (!isEnabled) return false
        val lowerUrl = url.lowercase()
        for (domain in blockedDomains) {
            if (lowerUrl.contains(domain)) return true
        }
        for (pattern in blockedPatterns) {
            if (lowerUrl.contains(pattern)) return true
        }
        return false
    }

    fun getBlockedCount(): Int = prefs.getInt("blocked_count", 0)

    fun incrementBlockedCount() {
        prefs.edit().putInt("blocked_count", getBlockedCount() + 1).apply()
    }
}
