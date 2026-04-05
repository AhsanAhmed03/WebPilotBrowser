package com.browser.app.managers

import android.content.Context
import android.content.SharedPreferences
import com.browser.app.models.HistoryEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("browser_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val KEY = "history_entries"
    private val MAX_ENTRIES = 500

    fun addEntry(entry: HistoryEntry) {
        val list = getAll().toMutableList()
        list.removeAll { it.url == entry.url }
        list.add(0, entry)
        if (list.size > MAX_ENTRIES) list.subList(MAX_ENTRIES, list.size).clear()
        save(list)
    }

    fun getAll(): List<HistoryEntry> {
        val json = prefs.getString(KEY, "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<HistoryEntry>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    fun remove(url: String) {
        val list = getAll().toMutableList()
        list.removeAll { it.url == url }
        save(list)
    }

    fun clearAll() {
        prefs.edit().remove(KEY).apply()
    }

    private fun save(list: List<HistoryEntry>) {
        prefs.edit().putString(KEY, gson.toJson(list)).apply()
    }
}
