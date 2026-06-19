package com.glufree.ktor.configure

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.glufree.ktor.Database")

fun Application.configureDatabase() {
    val dbHost = System.getenv("DATABASE_HOST") ?: "127.0.0.1"
    val dbPort = System.getenv("DATABASE_PORT") ?: "5432"
    val dbUser = System.getenv("DATABASE_USERNAME") ?: "admin"
    val dbPassword = System.getenv("DATABASE_PASSWORD") ?: "admin"
    val defaultDbName = "glutenfree"
    val dbName = System.getenv("DATABASE_NAME") ?: defaultDbName

    val jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName"

    logger.info("Connecting to Database: $jdbcUrl as user '$dbUser'...")

    val config = HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        this.jdbcUrl = jdbcUrl
        username = dbUser
        password = dbPassword
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    try {
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        logger.info("Database connection established successfully.")
    } catch (e: Exception) {
        logger.error("Failed to connect to database at $jdbcUrl: ${e.message}", e)
        throw e
    }
}
