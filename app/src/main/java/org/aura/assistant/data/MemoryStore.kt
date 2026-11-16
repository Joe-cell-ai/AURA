package org.aura.assistant.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.aura.assistant.security.SecureSettings

data class MemoryRecord(val id: Long, val content: String, val updatedAt: Long)

/** Explicitly user-managed memory; record contents are authenticated-encrypted at rest. */
class MemoryStore(context: Context, private val secure: SecureSettings) :
    SQLiteOpenHelper(context, "aura_memory.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE memories (id INTEGER PRIMARY KEY AUTOINCREMENT, content_enc TEXT NOT NULL, updated_at INTEGER NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
                db.execSQL("CREATE TABLE memories_secure (id INTEGER PRIMARY KEY AUTOINCREMENT, content_enc TEXT NOT NULL, updated_at INTEGER NOT NULL)")
                db.rawQuery("SELECT id, content, updated_at FROM memories", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val values = ContentValues().apply {
                            put("id", cursor.getLong(0))
                            put("content_enc", secure.encryptValue(cursor.getString(1)))
                            put("updated_at", cursor.getLong(2))
                        }
                        db.insertOrThrow("memories_secure", null, values)
                    }
                }
                db.execSQL("DROP TABLE memories")
                db.execSQL("ALTER TABLE memories_secure RENAME TO memories")
        }
    }

    fun all(): List<MemoryRecord> {
        val result = mutableListOf<MemoryRecord>()
        readableDatabase.rawQuery("SELECT id, content_enc, updated_at FROM memories ORDER BY updated_at DESC", null).use { c ->
            while (c.moveToNext()) {
                val content = runCatching { secure.decryptValue(c.getString(1)) }
                    .getOrElse { "[This memory cannot be decrypted on this device]" }
                result += MemoryRecord(c.getLong(0), content, c.getLong(2))
            }
        }
        return result
    }

    fun save(id: Long?, rawContent: String): Long {
        val content = rawContent.trim().take(500)
        require(MemoryText.isValid(content)) { "Memory must contain 3–500 characters." }
        val values = ContentValues().apply {
            put("content_enc", secure.encryptValue(content))
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

    fun exportText(): String = all().joinToString("\n") { "- ${it.content}" }

    fun context(maxItems: Int = 12, maxChars: Int = 2000): String =
        all().take(maxItems.coerceIn(1, 20)).joinToString("\n") { it.content }
            .take(maxChars.coerceIn(100, 4000))
}

object MemoryText {
    fun isValid(value: String): Boolean = value.trim().length in 3..500
}
