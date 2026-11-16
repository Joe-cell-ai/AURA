package org.aura.assistant.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class StoredMessage(val id: Long, val role: String, val content: String, val createdAt: Long)

class ConversationStore(context: Context) : SQLiteOpenHelper(context, "aura_conversations.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE messages (id INTEGER PRIMARY KEY AUTOINCREMENT, role TEXT NOT NULL, content TEXT NOT NULL, created_at INTEGER NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun add(role: String, content: String) {
        writableDatabase.insert("messages", null, ContentValues().apply {
            put("role", role); put("content", content); put("created_at", System.currentTimeMillis())
        })
    }

    fun recent(limit: Int = 30): List<StoredMessage> {
        val result = mutableListOf<StoredMessage>()
        readableDatabase.rawQuery(
            "SELECT id, role, content, created_at FROM messages ORDER BY id DESC LIMIT ?",
            arrayOf(limit.coerceIn(1, 100).toString())
        ).use { cursor ->
            while (cursor.moveToNext()) result += StoredMessage(
                cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3)
            )
        }
        return result.reversed()
    }

    fun clear() { writableDatabase.delete("messages", null, null) }
}
