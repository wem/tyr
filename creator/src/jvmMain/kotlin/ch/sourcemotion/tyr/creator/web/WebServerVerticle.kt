package ch.sourcemotion.tyr.creator.web

import ch.sourcemotion.tyr.creator.VerticleWithOptions
import ch.sourcemotion.tyr.creator.config.WebServerConfig
import io.vertx.ext.web.Router

class WebServerVerticle : VerticleWithOptions<WebServerConfig>(WebServerConfig::class) {

    override suspend fun start() {
        super.start()

        val router = Router.router(vertx)
    }
}