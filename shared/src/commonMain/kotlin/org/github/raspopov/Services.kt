package org.github.raspopov

data class User(val name: String)

class UserServiceImpl {
    fun getUserOrNull(name: String): User? {
        return User(name)
    }

    fun prepareHelloMessage(user: User?): String {
        return user?.name ?: "Stranger"
    }

}