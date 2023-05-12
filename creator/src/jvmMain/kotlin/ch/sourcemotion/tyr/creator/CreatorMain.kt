package ch.sourcemotion.tyr.creator

import ch.sourcemotion.tyr.creator.codec.registerLocalCodec
import ch.sourcemotion.tyr.creator.config.configureObjectMapper
import ch.sourcemotion.tyr.creator.config.loadConfiguration
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.vertxOptionsOf
import mu.KLogging

class CreatorMain {
    companion object : KLogging() {
        @JvmStatic
        fun main(args: Array<String>) {
            configureObjectMapper()
            val vertxOptions = vertxOptionsOf(preferNativeTransport = true)

            val vertx = Vertx.vertx(vertxOptions)
            vertx.eventBus().registerLocalCodec()

            vertx.loadConfiguration()
                .onSuccess { creatorConfig ->
                    vertx.deployVerticle(
                        CreatorVerticle::class.java,
                        deploymentOptionsOf(
                            config = JsonObject.mapFrom(
                                CreatorVerticle.Options(vertxOptions.eventLoopPoolSize, creatorConfig)
                            )
                        )
                    ).onSuccess {
                        logger.info { "Creator deployed" }
                    }.onFailure { cause ->
                        logger.error(cause) { "Failed to deploy creator, so stop" }
                        vertx.stop()
                    }
                }.onFailure { cause ->
                    logger.error(cause) { "Failed to load configuration, so stop creator" }
                    vertx.stop()
                }
        }

        private fun Vertx.stop() {
            close()
                .onSuccess { logger.info { "Creator stopped" } }
                .onFailure { failure -> logger.warn(failure) { "Creator stop failed" } }
        }
    }
}