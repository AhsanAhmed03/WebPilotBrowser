package com.browser.app.models

data class FeedItem(
    val title: String,
    val description: String = "",
    val link: String = "",
    val pubDate: String = "",
    val type: FeedType = FeedType.NEWS
)

enum class FeedType { NEWS, TRENDING }
