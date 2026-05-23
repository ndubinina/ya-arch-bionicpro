package ru.example.bionicpro_auth.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class PkceStore {
    val pkce = ConcurrentHashMap<String, String>()

    fun save(state: String, verifier: String) {
        pkce[state] = verifier
    }

    fun get(state: String): String? =
        pkce[state]

    fun remove(state: String) =
        pkce.remove(state)
}