package ru.example.bionicpro_auth.service

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant
import java.util.UUID

@Component
class SessionFilter(
    private val sessionStore: SessionStore,
    private val keycloakService: KeycloakService
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        return path.startsWith("/auth/") || "OPTIONS".equals(request.method, ignoreCase = true)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val sessionId =
            request.cookies?.find { it.name == "SESSION_ID" }?.value

        if (sessionId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }

        val session = sessionStore.get(sessionId)

        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }

        if (Instant.now().isAfter(session.expiresAt)) {

            val refreshed =
                keycloakService.refresh(session.refreshToken)

            session.accessToken = refreshed.accessToken
            session.refreshToken = refreshed.refreshToken
            session.expiresAt =
                Instant.now().plusSeconds(refreshed.expiresIn)
        }

        val newSessionId = UUID.randomUUID().toString()

        sessionStore.remove(sessionId)
        sessionStore.save(session.copy(sessionId = newSessionId))

        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authentication = UsernamePasswordAuthenticationToken(
            "user",
            null,
            authorities
        )

        SecurityContextHolder.getContext().authentication = authentication

        val cookie = ResponseCookie.from("SESSION_ID", newSessionId)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())

        chain.doFilter(request, response)
    }
}