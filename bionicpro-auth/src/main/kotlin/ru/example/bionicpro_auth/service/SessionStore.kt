package ru.example.bionicpro_auth.service

import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class SessionStore {
    val sessions = ConcurrentHashMap<String, Session>()

    fun save(session: Session) {
        sessions[session.sessionId] = session
    }

    fun get(id: String): Session? =
        sessions[id]

    fun remove(id: String) =
        sessions.remove(id)
}

data class Session(
    var sessionId: String,
    var accessToken: String,
    var refreshToken: String,
    var expiresAt: Instant,
)