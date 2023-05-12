package ch.sourcemotion.tyr.creator.config

import ch.sourcemotion.tyr.creator.exception.CreatorException
import io.vertx.config.ConfigRetriever
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.kotlin.config.configRetrieverOptionsOf
import io.vertx.kotlin.config.configStoreOptionsOf
import io.vertx.kotlin.core.json.jsonObjectOf
import mu.KotlinLogging

const val CONFIG_FILE_PATH_ENV_VAR = "CREATOR_CONFIG_PATH"

private val logger = KotlinLogging.logger("Configuration")

fun Vertx.loadConfiguration(): Future<CreatorConfig> {
    val configFilePath = System.getenv(CONFIG_FILE_PATH_ENV_VAR)
    val retrieverOptions = if (configFilePath != null) {
        logger.info { "Load creator configuration from file '$configFilePath'" }
        configRetrieverOptionsOf(
            includeDefaultStores = false,
            scanPeriod = -1,
            stores = listOf(
                configStoreOptionsOf(
                    type = "file",
                    format = "yaml",
                    config = jsonObjectOf("path" to configFilePath)
                )
            )
        )
    } else {
        logger.info { "Load creator configuration from env / sys vars" }
        configRetrieverOptionsOf(
            includeDefaultStores = false,
            scanPeriod = -1, stores = listOf(
                configStoreOptionsOf(type = "env"), configStoreOptionsOf(type = "sys")
            )
        )
    }

    return ConfigRetriever.create(
        Vertx.vertx(), retrieverOptions
    ).config.compose { configJson ->
        runCatching { Future.succeededFuture(configJson.mapTo(CreatorConfig::class.java)) }.getOrElse { cause ->
            Future.failedFuture(
                CreatorConfigurationException("Failed to parse configuration '${configJson.encodePrettily()}'", cause)
            )
        }
    }
}

class CreatorConfigurationException(message: String, cause: Throwable) : CreatorException(message, cause)