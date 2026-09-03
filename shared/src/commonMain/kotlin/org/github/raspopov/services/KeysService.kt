package org.github.raspopov.services

import org.github.raspopov.cache.DatabaseDriverFactory
import org.github.raspopov.cache.PublicKeysDatabase
import org.github.raspopov.domain.Chat
import org.github.raspopov.views.MasterPasswordViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
interface KeysService {
    fun addKeyFor(user: Uuid, account: Uuid, username: String, key: ByteArray)
    fun getPublicKeyOf(user: Uuid): ByteArray
    fun allUsers(account: Uuid): List<Chat>
    fun delete(userId: Uuid, accountId: Uuid)
}

class KeysServiceImpl(
    databaseDriverFactory: DatabaseDriverFactory,
    private val masterPasswordViewModel: MasterPasswordViewModel,
) : KeysService {

    private val keysDatabase: PublicKeysDatabase =
        PublicKeysDatabase(databaseDriverFactory, masterPasswordViewModel)

    override fun addKeyFor(user: Uuid, account: Uuid, username: String, key: ByteArray) {
        keysDatabase.addPublicKey(user, account, username, key)
    }

    override fun getPublicKeyOf(user: Uuid): ByteArray {
        return keysDatabase.getPublicKey(user)
    }

    override fun allUsers(account: Uuid): List<Chat> {
        return keysDatabase.allUsers(account)
    }

    override fun delete(userId: Uuid, accountId: Uuid) {
        keysDatabase.delete(userId, accountId)
    }
}