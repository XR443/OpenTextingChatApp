package org.github.raspopov.services

import io.ktor.utils.io.core.toByteArray
import org.github.raspopov.domain.Chat
import org.github.raspopov.domain.Message
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
interface ChatService {
  suspend  fun getChats(): List<Chat>
}
