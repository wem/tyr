package ch.sourcemotion.tyr.creator

import ch.sourcemotion.tyr.creator.config.configureObjectMapper
import ch.sourcemotion.tyr.creator.config.loadConfiguration
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.vertxOptionsOf
import mu.KLogging

class Main {
    companion object : KLogging() {
        @JvmStatic
        fun main(args: Array<String>) {
            configureObjectMapper()
            val vertx = Vertx.vertx(vertxOptionsOf(preferNativeTransport = true))

            vertx.loadConfiguration()
                .onSuccess { creatorConfig ->
                    vertx.deployVerticle(
                        CreatorVerticle::class.java,
                        deploymentOptionsOf(config = JsonObject.mapFrom(creatorConfig))
                    ).onSuccess {
                        logger.info { "Creator deployed" }
                    }.onFailure { cause ->
                        logger.error(cause) { "Failed to deploy creator, so stop" }
                        vertx.close()
                    }
                }.onFailure { cause ->
                    logger.error(cause) { "Failed to load configuration, so stop creator" }
                    vertx.close()
                }

        }
    }
}