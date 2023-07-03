package ch.sourcemotion.tyr.creator

import ch.sourcemotion.tyr.creator.CreatorVerticle.Options
import ch.sourcemotion.tyr.creator.config.CreatorConfig
import ch.sourcemotion.tyr.creator.database.DatabaseVersioning
import ch.sourcemotion.tyr.creator.domain.repository.FileInfoRepository
import ch.sourcemotion.tyr.creator.domain.repository.QuizCategoryRepository
import ch.sourcemotion.tyr.creator.domain.repository.QuizRepository
import ch.sourcemotion.tyr.creator.domain.repository.QuizStageRepository
import ch.sourcemotion.tyr.creator.domain.service.FileServiceVerticle
import ch.sourcemotion.tyr.creator.domain.service.QuizCategoryServiceVerticle
import ch.sourcemotion.tyr.creator.domain.service.QuizServiceVerticle
import ch.sourcemotion.tyr.creator.domain.service.QuizStageServiceVerticle
import ch.sourcemotion.tyr.creator.domain.storage.FileStorage
import ch.sourcemotion.tyr.creator.ext.createPool
import ch.sourcemotion.tyr.creator.ext.getOrCreateByFactory
import ch.sourcemotion.tyr.creator.ext.shareFactory
import ch.sourcemotion.tyr.creator.web.WebServerVerticle
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KLogging

class CreatorVerticle : VerticleWithOptions<Options>(Options::class) {

    private companion object : KLogging()

    override suspend fun start() {
        super.start()

        DatabaseVersioning(vertx, options.config.postgres).updateDatabase()
        sharePgPool()
        shareRepositories()
        deployServiceLayer()
        deployWebServer()
    }

    private suspend fun deployWebServer() {
        vertx.deployVerticle(
            WebServerVerticle::class.java,
            deploymentOptionsOf(config = JsonObject.mapFrom(options.config.webServer))
        ).await()
        logger.info { "Web server deployed" }
    }

    private suspend fun deployServiceLayer() {
        coroutineScope {
            launch {
                vertx.deployVerticle(QuizServiceVerticle::class.java, deploymentOptionsOf()).await()
                logger.info { "Quiz service deployed" }
            }
            launch {
                vertx.deployVerticle(QuizStageServiceVerticle::class.java, deploymentOptionsOf()).await()
                logger.info { "Quiz stage service deployed" }
            }
            launch {
                vertx.deployVerticle(QuizCategoryServiceVerticle::class.java, deploymentOptionsOf()).await()
                logger.info { "Quiz category service deployed" }
            }
            launch {
                vertx.deployVerticle(FileServiceVerticle::class.java, deploymentOptionsOf()).await()
                logger.info { "File service deployed" }
            }
        }
        logger.info { "Service layer deployed" }
    }

    private fun shareRepositories() {
        vertx.shareFactory { QuizRepository(vertx.getOrCreateByFactory()) }
        vertx.shareFactory { QuizStageRepository(vertx.getOrCreateByFactory()) }
        vertx.shareFactory { QuizCategoryRepository(vertx.getOrCreateByFactory()) }
        vertx.shareFactory { FileStorage.of(vertx, options.config.fileStorage) }
        vertx.shareFactory { FileInfoRepository(vertx.getOrCreateByFactory()) }
    }

    private fun sharePgPool() {
        vertx.shareFactory { options.config.postgres.createPool(vertx) }
    }

    data class Options(val eventLoops: Int, val config: CreatorConfig)
}