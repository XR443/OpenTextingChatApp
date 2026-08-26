package org.github.raspopov.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.github.raspopov.domain.Chat

// Creates the required serialization configuration for open polymorphism
internal val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ChatListRoute::class, ChatListRoute.serializer())
            subclass(ChatScreenRoute::class, ChatScreenRoute.serializer())
            subclass(AccountScreenRoute::class, AccountScreenRoute.serializer())
            subclass(MasterPasswordRoute::class, MasterPasswordRoute.serializer())
            subclass(PublicKeysScreenRoute::class, PublicKeysScreenRoute.serializer())
        }
    }
}

@Serializable
internal data object MasterPasswordRoute : NavKey

@Serializable
internal data object ChatListRoute : NavKey

@Serializable
internal data class ChatScreenRoute(val chat: Chat) : NavKey

@Serializable
internal data object AccountScreenRoute : NavKey
@Serializable
internal data object PublicKeysScreenRoute : NavKey
