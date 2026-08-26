package org.github.raspopov.domain

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
@Serializable
class Chat(
    val userId: Uuid,
    val name: String
) {
}