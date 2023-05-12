package ch.sourcemotion.tyr.creator.database

import ch.sourcemotion.tyr.creator.config.PostgresConfig
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import java.sql.DriverManager
import java.util.*

class DatabaseVersioning(private val vertx: Vertx, private val config: PostgresConfig) {
    private val liquibase: Liquibase = liquibaseOf(config)

    suspend fun updateDatabase() {
        vertx.executeBlocking<Unit> { promise ->
            try {
                liquibase.update(config.schemaVersion, Contexts())
                liquibase.close()
                promise.complete()
            } catch (cause: Exception) {
                promise.fail(cause)
            }
        }.await()
    }
}

private fun liquibaseOf(config: PostgresConfig): Liquibase {
    val jdbcConnection = jdbcConnectionOf(config)
    return Liquibase(config.schemaDefinitionFilePath, ClassLoaderResourceAccessor(), jdbcConnection)
}

private fun jdbcConnectionOf(config: PostgresConfig): JdbcConnection {
    val properties = jdbcPropertiesOf(config)
    val url = jdbcUrlOf(config)
    return JdbcConnection(DriverManager.getConnection(url, properties))
}

private fun jdbcPropertiesOf(config: PostgresConfig) = Properties().apply {
    put("user", config.userName)
    put("password", config.password)
    put("ssl", "${config.ssl}")
}


private fun jdbcUrlOf(config: PostgresConfig) =
    "jdbc:postgresql://${config.host}:${config.port}/${config.database}"