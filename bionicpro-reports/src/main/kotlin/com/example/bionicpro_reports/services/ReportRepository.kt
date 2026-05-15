package com.example.bionicpro_reports.services

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ReportRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun getReport(email: String): List<Report> {
        val sql = """
            SELECT
                user_id,
                name,
                email,
                country,
                report_day,
                signals_count,
                avg_amplitude,
                avg_duration,
                total_duration
            FROM user_daily_reports_mart
            WHERE email = ?
            ORDER BY report_day
        """

        return jdbcTemplate.query(sql, arrayOf(email)) { rs, _ ->
            Report(
                userId = rs.getInt("user_id"),
                name = rs.getString("name"),
                email = rs.getString("email"),
                country = rs.getString("country"),
                reportDay = rs.getString("report_day"),
                signalsCount = rs.getInt("signals_count"),
                avgAmplitude = rs.getDouble("avg_amplitude"),
                avgDuration = rs.getDouble("avg_duration"),
                totalDuration = rs.getLong("total_duration")
            )
        }
    }
}