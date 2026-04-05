package com.browser.app.models

data class SuggestionItem(
    val text: String,
    val type: SuggestionType,
    val url: String? = null
)

enum class SuggestionType {
    SEARCH,
    BOOKMARK,
    HISTORY,
    TRENDING
}
