package org.github.raspopov.cache

import org.github.raspopov.domain.Account
import org.github.raspopov.domain.Chat
import org.github.raspopov.domain.MasterKeys
import org.github.raspopov.extensions.toDomain
import org.github.raspopov.views.MasterPasswordViewModel
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
internal class PublicKeysDatabase(
    databaseDriverFactory: DatabaseDriverFactory,
    private val masterPasswordViewModel: MasterPasswordViewModel,
) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val crud = database.publicKeysCrudQueries

    private val masterKeys: MasterKeys?
        get() = masterPasswordViewModel.uiState.value.keys

    internal fun addPublicKey(user: Uuid, account: Uuid, username: String, key: ByteArray) {
        crud.insertOrUpdate(user.toString(), account.toString(), username, key)
    }

    internal fun getPublicKey(user: Uuid): ByteArray {
        return crud.selectByUserId(user.toString()).executeAsOne().public_key
    }

    fun allUsers(account: Uuid): List<Chat> {
        return crud.selectAllUsersForAccount(account.toString()).executeAsList()
            .map { Chat(Uuid.parse(it.user_id), it.username) }
    }

    fun delete(userId: Uuid, accountId: Uuid) {
        crud.deleteByIdAndAccountId(userId.toString(), accountId.toString())
    }

}