package org.aura.assistant.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class MemoryEntry(val id: Long, val fact: String, val updatedAt: Long)

/** Explicitly user-managed facts. Nothing is added automatically. */
class MemoryStore(context: Context) : SQLiteOpenHelper(context, "aura_memory.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE memories (id INTEGER PRIMARY KEY AUTOINCREMENT, fact TEXT NOT NULL, updated_at INTEGER NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun all(): List<MemoryEntry> {
        val entries = mutableListOf<MemoryEntry>()
        readableDatabase.rawQuery(
            "SELECT id, fact, updated_at FROM memories ORDER BY updated_at DESC LIMIT 100",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) entries += MemoryEntry(
                cursor.getLong(0), cursor.getString(1), cursor.getLong(2)
            )
        }
        return entries
    }

    fun add(fact: String): Long {
        val clean = fact.trim().take(500)
        require(clean.isNotBlank()) { "Memory cannot be empty." }
        return writableDatabase.insertOrThrow("memories", null, ContentValues().apply {
            put("fact", clean)
            put("updated_at", System.currentTimeMillis())
        })
    }

    fun update(id: Long, fact: String) {
        val clean = fact.trim().take(500)
        require(clean.isNotBlank()) { "Memory cannot be empty." }
        writableDatabase.update("memories", ContentValues().apply {
            put("fact", clean)
            put("updated_at", System.currentTimeMillis())
        }, "id = ?", arrayOf(id.toString()))
    }

    fun delete(id: Long) {
        writableDatabase.delete("memories", "id = ?", arrayOf(id.toString()))
    }

    fun clear() {
        writableDatabase.delete("memories", null, null)
    }
}
