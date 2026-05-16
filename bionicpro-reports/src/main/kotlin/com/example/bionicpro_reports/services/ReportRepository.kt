package com.example.bionicpro_reports.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ReportRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    @Value("\${report.version}")
    var version: Int = 1

    fun getReport(email: String): List<Report> {
        return when (version) {
            2 -> getReport2(email)
            else -> getReport1(email)
        }
    }

    fun getReport1(email: String): List<Report> {
        println("getReport1: START")
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

    fun getReport2(email: String): List<Report> {
        println("getReport2: START")
        val sql = """
            SELECT
                r.user_id AS user_id,
                c.name AS name,
                c.email AS email,
                c.country AS country,
                r.report_day AS report_day,
                r.signals_count AS signals_count,
                r.avg_amplitude AS avg_amplitude,
                r.avg_duration AS avg_duration,
                r.total_duration AS total_duration
            FROM reports_agg r
            INNER JOIN customers_cdc c
                ON r.user_id = c.user_id
            WHERE c.email = ?
            ORDER BY r.report_day;
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