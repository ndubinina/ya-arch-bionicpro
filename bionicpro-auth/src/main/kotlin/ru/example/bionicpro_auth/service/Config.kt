package ru.example.bionicpro_auth.service

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class Config {
    @Bean
    fun webClient(): WebClient =
        WebClient.builder().build()
}