package io.github.ivruix.filemanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class FileHashDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "file_hash.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "file_hashes"
        private const val COLUMN_PATH = "path"
        private const val COLUMN_HASH = "hash"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $TABLE_NAME ($COLUMN_PATH TEXT PRIMARY KEY, $COLUMN_HASH TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertFileHash(file: File) {
        val hash = calculateHash(file)
        val values = ContentValues().apply {
            put(COLUMN_PATH, file.path)
            put(COLUMN_HASH, hash)
        }
        writableDatabase.insertWithOnConflict(
            TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getFileHash(file: File): String? {
        val cursor = readableDatabase.query(
            TABLE_NAME,
            arrayOf(COLUMN_HASH),
            "$COLUMN_PATH = ?",
            arrayOf(file.canonicalPath),
            null,
            null,
            null
        )
        // If hash exists for this file return it, otherwise return null
        return if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndex(COLUMN_HASH)) else null
    }

    fun calculateHash(file: File): String {
        // Calculate hash using SHA-256 algorithm
        val md = MessageDigest.getInstance("SHA-256")
        val inputStream = FileInputStream(file)
        val buffer = ByteArray(8192)
        var bytesRead = inputStream.read(buffer)
        while (bytesRead != -1) {
            md.update(buffer, 0, bytesRead)
            bytesRead = inputStream.read(buffer)
        }
        inputStream.close()
        val digest = md.digest()
        return digest.joinToString("") { "%02x".format(it) }
    }
}
