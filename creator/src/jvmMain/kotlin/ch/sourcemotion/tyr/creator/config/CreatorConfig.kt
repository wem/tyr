package ch.sourcemotion.tyr.creator.config

data class CreatorConfig(
    val webServer: WebServerConfig,
    val postgres: PostgresConfig
)

data class WebServerConfig(val port: Int)

data class PostgresConfig(
    val userName: String,
    val password: String,
    val host: String,
    val port: Int,
    val ssl: Boolean,
    val database: String,
    val schemaDefinitionFilePath: String = "/database/database-change-log.yaml",
    val schemaVersion: String = "0.1", // Liquibase tag. Usually this should be the latest version and changes on schema changes
    val maxConnectionsPerThread: Int = 5,
)
