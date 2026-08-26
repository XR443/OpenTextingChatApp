package org.github.raspopov.cache

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import java.io.File

class AndroidDatabaseDriverFactory(private val context: Context) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
//        val dbFile = File(context.filesDir, "chatapp.db")
        return AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = context,
//            name = dbFile.absolutePath  // Используем полный путь
            name = "chatapp.db"
        )
    }
}