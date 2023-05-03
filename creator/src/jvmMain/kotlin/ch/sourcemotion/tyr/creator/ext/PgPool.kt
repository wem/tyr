package ch.sourcemotion.tyr.creator.ext

import ch.sourcemotion.tyr.creator.config.PostgresConfig
import io.vertx.core.Vertx
import io.vertx.kotlin.pgclient.pgConnectOptionsOf
import io.vertx.kotlin.sqlclient.poolOptionsOf
import io.vertx.pgclient.PgPool

fun PostgresConfig.createPool(vertx: Vertx): PgPool {
    val connectionOptions = pgConnectOptionsOf(
        host = host,
        port = port,
        user = userName,
        password = password,
        database = database,
    )

    val poolOptions = poolOptionsOf(maxSize = maxConnectionsPerThread)
    return PgPool.pool(vertx, connectionOptions, poolOptions)
}