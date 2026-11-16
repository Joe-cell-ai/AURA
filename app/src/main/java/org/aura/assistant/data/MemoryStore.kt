package org.aura.assistant.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class MemoryRecord(val id: Long, val content: String, val updatedAt: Long)

/** Explicitly user-managed memory. Nothing is extracted from chats automatically. */
class MemoryStore(context: Context) : SQLiteOpenHelper(context, "aura_memory.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE memories (id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT NOT NULL, updated_at INTEGER NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun all(): List<MemoryRecord> {
        val result = mutableListOf<MemoryRecord>()
        readableDatabase.rawQuery("SELECT id, content, updated_at FROM memories ORDER BY updated_at DESC", null).use { c ->
            while (c.moveToNext()) result += MemoryRecord(c.getLong(0), c.getString(1), c.getLong(2))
        }
        return result
    }

    fun save(id: Long?, rawContent: String): Long {
        val content = rawContent.trim().take(500)
        require(MemoryText.isValid(content)) { "Memory must contain 3–500 characters." }
        val values = ContentValues().apply {
            put("content", content)
            put("updated_at", System.currentTimeMillis())
        }
        return if (id == null) writableDatabase.insertOrThrow("memories", null, values)
        else {
            val updated = writableDatabase.update("memories", values, "id=?", arrayOf(id.toString()))
            require(updated == 1) { "Memory no longer exists. Refresh and try again." }
            id
        }
    }

    fun delete(id: Long) { writableDatabase.delete("memories", "id=?", arrayOf(id.toString())) }
    fun clear() { writableDatabase.delete("memories", null, null) }

    fun context(maxItems: Int = 12, maxChars: Int = 2000): String =
        all().take(maxItems.coerceIn(1, 20)).joinToString("\n") { it.content }
            .take(maxChars.coerceIn(100, 4000))
}

object MemoryText {
    fun isValid(value: String): Boolean = value.trim().length in 3..500
}
