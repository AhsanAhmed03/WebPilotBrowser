package com.browser.app.managers

import android.content.Context
import android.content.SharedPreferences
import com.browser.app.models.Bookmark
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BookmarkManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("browser_bookmarks", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val KEY = "bookmarks"

    fun add(bookmark: Bookmark) {
        val list = getAll().toMutableList()
        list.removeAll { it.url == bookmark.url }
        list.add(0, bookmark)
        save(list)
    }

    fun getAll(): List<Bookmark> {
        val json = prefs.getString(KEY, "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<Bookmark>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    fun remove(url: String) {
        val list = getAll().toMutableList()
        list.removeAll { it.url == url }
        save(list)
    }

    fun isBookmarked(url: String): Boolean = getAll().any { it.url == url }

    fun clearAll() {
        prefs.edit().remove(KEY).apply()
    }

    private fun save(list: List<Bookmark>) {
        prefs.edit().putString(KEY, gson.toJson(list)).apply()
    }
}
