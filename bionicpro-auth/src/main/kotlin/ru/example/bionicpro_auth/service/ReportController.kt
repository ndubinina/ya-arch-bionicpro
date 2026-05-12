package ru.example.bionicpro_auth.service

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.Instant
import java.util.UUID

@RestController
@Validated
@RequestMapping("\${api.base-path:}")
class ReportController(
    private val keycloakService: KeycloakService,
    private val sessionStore: SessionStore,
) {

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/check/session"]
    )
    fun checkSession(): ResponseEntity<Void> {
        return ResponseEntity.status(200).build()
    }


    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/auth/login"]
    )
    fun login(): ResponseEntity<Void> {
        val url = keycloakService.buildLoginUrl()

        return ResponseEntity.status(302)
            .location(URI.create(url))
            .build()
    }

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/auth/callback"]
    )
    fun callback(
        @RequestParam state: String,
        @RequestParam code: String,
        response: HttpServletResponse
    ): ResponseEntity<String> {
        val tokens = keycloakService.exchangeCode(state, code)

        val sessionId = UUID.randomUUID().toString()

        sessionStore.save(
            Session(
                sessionId,
                tokens.accessToken,
                tokens.refreshToken,
                Instant.now().plusSeconds(tokens.expiresIn)
            )
        )

        val cookie = ResponseCookie.from("SESSION_ID", sessionId)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())

        return ResponseEntity.status(302)
            .location(URI.create("http://localhost:3000"))
            .build()
    }
}