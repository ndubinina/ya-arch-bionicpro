package com.example.bionicpro_reports.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.util.Base64

@RestController
class ReportsController(
    private val repository: ReportRepository,
    private val objectMapper: ObjectMapper
) {

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/reports"]
    )
    fun getReport(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String
    ): ResponseEntity<String> {
        println("getReport: ENTER")
        val token = authHeader.removePrefix("Bearer ").trim()

        val userEmail = extractUserEmail(token)

        val report = objectMapper.writeValueAsString(repository.getReport(userEmail))
        println("getReport: report = $report")

        return ResponseEntity.ok(report)
    }

    fun extractUserEmail(token: String): String {

        val parts = token.split(".")

        require(parts.size == 3) { "Invalid JWT" }

        val payloadJson =
            String(Base64.getUrlDecoder().decode(parts[1]))

        val mapper = objectMapper

        val payload: Map<String, Any> =
            mapper.readValue(payloadJson, Map::class.java) as Map<String, Any>

        return payload["email"] as String
    }
}