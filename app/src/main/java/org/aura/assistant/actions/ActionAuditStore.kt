package org.aura.assistant.actions

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class ActionAuditRecord(val id: Long, val action: String, val outcome: String, val createdAt: Long)

/** Minimal local audit log; never stores tokens, prompts or full device payloads. */
class ActionAuditStore(context: Context) : SQLiteOpenHelper(context, "aura_action_audit.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE action_log (id INTEGER PRIMARY KEY AUTOINCREMENT, action TEXT NOT NULL, outcome TEXT NOT NULL, created_at INTEGER NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun record(action: String, outcome: String) {
        writableDatabase.insert("action_log", null, ContentValues().apply {
            put("action", action.take(80))
            put("outcome", outcome.take(80))
            put("created_at", System.currentTimeMillis())
        })
        writableDatabase.execSQL("DELETE FROM action_log WHERE id NOT IN (SELECT id FROM action_log ORDER BY id DESC LIMIT 100)")
    }

    fun recent(): List<ActionAuditRecord> {
        val records = mutableListOf<ActionAuditRecord>()
        readableDatabase.rawQuery("SELECT id, action, outcome, created_at FROM action_log ORDER BY id DESC LIMIT 100", null).use { c ->
            while (c.moveToNext()) records += ActionAuditRecord(c.getLong(0), c.getString(1), c.getString(2), c.getLong(3))
        }
        return records
    }

    fun clear() { writableDatabase.delete("action_log", null, null) }
}
