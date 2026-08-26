package org.github.raspopov

import org.github.raspopov.cache.DatabaseDriverFactory
import org.github.raspopov.cache.DesktopDatabaseDriverFactory
import org.github.raspopov.services.DbMessageCacheImpl
import org.github.raspopov.services.MessageCache
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import kotlin.uuid.ExperimentalUuidApi

@ExperimentalUuidApi
val desktopModule = module {
    includes(commonModule)

    single<DatabaseDriverFactory> {
        DesktopDatabaseDriverFactory()
    }
    single<MessageCache> {
        DbMessageCacheImpl(
            get(),
            get(),
            get(),
        )
    }
}