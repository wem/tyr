package ch.sourcemotion.tyr.creator.web

import ch.sourcemotion.tyr.creator.VerticleWithOptions
import ch.sourcemotion.tyr.creator.config.WebServerConfig
import ch.sourcemotion.tyr.creator.web.resource.CreatorContext
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.kotlin.coroutines.await
import mu.KLogging

class WebServerVerticle : VerticleWithOptions<WebServerConfig>(WebServerConfig::class) {

    private companion object : KLogging()

    override suspend fun start() {
        super.start()

        val server = vertx.createHttpServer()

        val mainRouter = Router.router(vertx)
        mainRouter.route().handler(BodyHandler.create(true).setUploadsDirectory(options.fileUploadFolder))
        if (options.develMode) {
            mainRouter.route().handler(CorsHandler.create())
            logger.info { "CORS enabled" }
        }
        CreatorContext(vertx, this).deploy(mainRouter)

        server.requestHandler(mainRouter)
        server.listen(options.port).await()
    }
}