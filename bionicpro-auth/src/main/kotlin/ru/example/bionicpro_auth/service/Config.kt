package ru.example.bionicpro_auth.service

import io.minio.MinioClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class Config {
    @Bean
    fun webClient(): WebClient =
        WebClient.builder().build()

    @Bean
    fun minioClient(): MinioClient =
        MinioClient.builder()
            .endpoint("http://minio:9000")
            .credentials("minio_user", "minio_password")
            .build()
}