package org.github.raspopov.cache

import org.github.raspopov.domain.Account
import org.github.raspopov.domain.MasterKeys
import org.github.raspopov.extensions.toDomain
import org.github.raspopov.views.MasterPasswordViewModel
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
internal class AccountDatabase(
    databaseDriverFactory: DatabaseDriverFactory,
    private val masterPasswordViewModel: MasterPasswordViewModel,
) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val queries = database.accountsQueriesQueries
    private val crud = database.accountsCRUDQueries

    private val masterKeys: MasterKeys?
        get() = masterPasswordViewModel.uiState.value.keys


    internal fun accountsExists(): Boolean {
        return crud.existsAccounts().executeAsOne()

    }

    internal fun getAccounts(): List<Account> {
        return queries.getAllAccountsSorted().executeAsList().asSequence()
            .toDomain()
            .toList()
    }

    internal fun insertNewAccount(account: Account) {
        val count: Long = queries.getAccountsCount().executeAsOne()

        crud.insertAccount(
            account.id.toString(),
            account.username,
            account.host,
            account.password, // todo пароль уже должен быть закрыт
            if (count == 0L) 1 else 0,
            Clock.System.now().toEpochMilliseconds(),
            Clock.System.now().toEpochMilliseconds(),
            account.salt!!,
            account.privateKey!!, // todo ключ уже должен быть закрыт
            account.publicKey!! // todo ключ уже должен быть закрыт
        )
    }

    internal fun delete(accountId: Uuid): List<Account> {
        crud.deleteAccountById(accountId.toString())
        return getAccounts()
    }

    internal fun update(account: Account): List<Account> {
        crud.updateAccount(
            account.username,
            account.host,
            account.password, // todo пароль уже должен быть закрыт
            if (account.isMain) 1L else 0L,
            Clock.System.now().toEpochMilliseconds(),
            account.id.toString()
        )
        return getAccounts()
    }

    internal fun setMain(accountId: Uuid): List<Account> {
        crud.updateMainAccount(accountId.toString())
        return getAccounts()
    }
}