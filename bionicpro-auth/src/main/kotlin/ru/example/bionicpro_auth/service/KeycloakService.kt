package ru.example.bionicpro_auth.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*


@Component
@ConfigurationProperties(prefix = "keycloak")
class KeycloakProperties {
    lateinit var tokenUrl: String
    lateinit var authorizeUrl: String
    lateinit var redirectUri: String
    lateinit var clientId: String
}

@Service
class KeycloakService(
    private val restTemplate: RestTemplate = RestTemplate(),
    private val keycloakProperties: KeycloakProperties,
    private val pkceStore: PkceStore,
) {
    fun buildLoginUrl(): String {
        val state = UUID.randomUUID().toString()
        val verifier = generateCodeVerifier()
        val challenge = generateCodeChallenge(verifier)
        pkceStore.save(state, verifier)
        return UriComponentsBuilder
            .fromUriString(keycloakProperties.authorizeUrl)
            .queryParam("client_id", keycloakProperties.clientId)
            .queryParam("response_type", "code")
            .queryParam("scope", "openid")
            .queryParam("code_challenge", challenge)
            .queryParam("code_challenge_method", "S256")
            .queryParam("state", state)
            .queryParam("redirect_uri", keycloakProperties.redirectUri)
            .build()
            .toUriString()
    }



    fun exchangeCode(state: String, code: String): KeycloakTokenResponse {

        val body = LinkedMultiValueMap<String, String>()
        val code_verifier = pkceStore.get(state)

        body.add("grant_type", "authorization_code")
        body.add("code", code)
        body.add("code_verifier", code_verifier)
        body.add("client_id", keycloakProperties.clientId)
        body.add("redirect_uri", keycloakProperties.redirectUri)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val request = HttpEntity(body, headers)

        return restTemplate.postForObject(
            keycloakProperties.tokenUrl,
            request,
            KeycloakTokenResponse::class.java
        )!!
    }

    private fun generateCodeVerifier(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")

        val hash = digest.digest(
            verifier.toByteArray(StandardCharsets.US_ASCII)
        )

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(hash)
    }

    fun refresh(refreshToken: String): KeycloakTokenResponse {

        val body = LinkedMultiValueMap<String, String>()

        body.add("grant_type", "refresh_token")
        body.add("refresh_token", refreshToken)
        body.add("client_id", keycloakProperties.clientId)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val request = HttpEntity(body, headers)

        return restTemplate.postForObject(
            keycloakProperties.tokenUrl,
            request,
            KeycloakTokenResponse::class.java
        )!!
    }
}

data class KeycloakTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("expires_in")
    val expiresIn: Long,

    @JsonProperty("refresh_token")
    val refreshToken: String,

    @JsonProperty("token_type")
    val tokenType: String
)