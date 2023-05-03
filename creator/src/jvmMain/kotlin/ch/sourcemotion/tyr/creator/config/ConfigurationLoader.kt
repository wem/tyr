package ch.sourcemotion.tyr.creator.config

import ch.sourcemotion.tyr.creator.exception.CreatorException
import io.vertx.config.ConfigRetriever
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.kotlin.config.configRetrieverOptionsOf
import io.vertx.kotlin.config.configStoreOptionsOf


fun Vertx.loadConfiguration(): Future<CreatorConfig> {
    return ConfigRetriever.create(
        Vertx.vertx(), configRetrieverOptionsOf(
            includeDefaultStores = false, scanPeriod = -1, listOf(
                configStoreOptionsOf(type = "env"), configStoreOptionsOf(type = "sys")
            )
        )
    ).config.compose { configJson ->
        runCatching { Future.succeededFuture(configJson.mapTo(CreatorConfig::class.java)) }.getOrElse { cause ->
            Future.failedFuture(CreatorConfigurationException("Failed to parse configuration '${configJson.encodePrettily()}'", cause))
        }
    }
}

class CreatorConfigurationException(message: String, cause: Throwable) : CreatorException(message, cause)