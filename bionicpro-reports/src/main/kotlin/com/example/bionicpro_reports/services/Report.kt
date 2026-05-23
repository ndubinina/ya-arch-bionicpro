package com.example.bionicpro_reports.services

data class Report(
    val userId: Int,
    val name: String,
    val email: String,
    val country: String,
    val reportDay: String,
    val signalsCount: Int,
    val avgAmplitude: Double,
    val avgDuration: Double,
    val totalDuration: Long
)