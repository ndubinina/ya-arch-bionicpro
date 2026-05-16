package ru.example.bionicpro_auth.service

import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.StatObjectArgs
import io.minio.http.Method
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
import java.io.ByteArrayInputStream
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.TimeUnit

@RestController
@Validated
@RequestMapping("\${api.base-path:}")
class ReportController(
    private val keycloakService: KeycloakService,
    private val sessionStore: SessionStore,
    private val webClient: WebClient,
    private val minioClient: MinioClient,
) {
    private val bucket = "reports"

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

        val day = LocalDate.now()

        val path = "${authentication.principal as String}/$day.json"

        if (exists(path)) {
            println("return fron minio")
            return ResponseEntity.ok("http://localhost:8089/reports/$path")
        }

        val report = webClient.get()
            .uri("http://bionicpro-reports:8083/reports")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${(authentication.credentials as String)}")
            .retrieve()
            .toEntity(String::class.java)
            .block()!!.body
        println("report = $report")

        report?.let { upload(path, report) }

        val presignedUrl = minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket("reports")
                .`object`("$path")
                .expiry(5, TimeUnit.MINUTES)
                .build()
        )
        val cdnUrl = presignedUrl.replace("http://minio:9000", "http://localhost:8089")

        return ResponseEntity.ok(cdnUrl)
    }

    private fun exists(path: String): Boolean =
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(path)
                    .build()
            )
            true
        } catch (e: Exception) {
            false
        }

    private fun upload(path: String, content: String) {

        val stream = ByteArrayInputStream(content.toByteArray())

        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(path)
                .stream(stream, content.length.toLong(), -1)
                .contentType("application/json")
                .build()
        )
    }
}