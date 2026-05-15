package ru.example.bionicpro_auth.service

import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI
import java.time.Instant
import java.util.UUID

@RestController
@Validated
@RequestMapping("\${api.base-path:}")
class ReportController(
    private val keycloakService: KeycloakService,
    private val sessionStore: SessionStore,
    private val webClient: WebClient,
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

    @GetMapping("/reports")
    fun getReport(authentication: Authentication): ResponseEntity<String> {
        println("getReport START")
        println("authentication = $authentication")
        println("authentication.credentials = ${authentication.credentials}")
        val report = webClient.get()
            .uri("http://bionicpro-reports:8083/reports")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${(authentication.credentials as String)}")
            .retrieve()
            .toEntity(String::class.java)
            .block()!!.body
        println("report = $report")
        return ResponseEntity.ok(report)
    }
}