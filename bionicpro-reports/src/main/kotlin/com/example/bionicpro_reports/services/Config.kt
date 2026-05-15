package com.example.bionicpro_reports.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Config {
    @Bean
    fun objectMapper(): ObjectMapper? {
        return ObjectMapper()
    }
}