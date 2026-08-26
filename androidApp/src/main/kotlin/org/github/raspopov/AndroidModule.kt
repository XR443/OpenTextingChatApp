package org.github.raspopov

import org.github.raspopov.cache.AndroidDatabaseDriverFactory
import org.github.raspopov.cache.DatabaseDriverFactory
import org.github.raspopov.services.DbMessageCacheImpl
import org.github.raspopov.services.MessageCache
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
val androidModule = module {
    includes(commonModule)

    single<DatabaseDriverFactory> {
        AndroidDatabaseDriverFactory(androidContext())
    }
    single<MessageCache> {
        DbMessageCacheImpl(
            get(),
            get(),
            get(),
        )
    }
}