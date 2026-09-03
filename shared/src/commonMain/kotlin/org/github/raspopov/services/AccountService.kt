package org.github.raspopov.services

import org.github.raspopov.domain.Account
import org.github.raspopov.dto.AccessTokenDto
import org.github.raspopov.dto.UserInfoDto
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
interface AccountService {
    suspend fun login(): AccessTokenDto?

    suspend fun login(account: Account): AccessTokenDto?

    // todo password as CharArray
    suspend fun login(host: String, username: String, password: String): AccessTokenDto?

    suspend fun getInfo(userId: Uuid): UserInfoDto
    suspend fun getInfoAboutMe(account: Account? = null): UserInfoDto
    suspend fun getInfoAboutMe(host: String, username: String, password: String): UserInfoDto
}
