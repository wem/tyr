package ch.sourcemotion.tyr.creator.testing

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.await

interface VertxWebTest {

    suspend fun withTestWebServer(vertx: Vertx, block: Router.() -> Unit) : Int {
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create(true))
        block(router)
        val server = vertx.createHttpServer()
        return server.requestHandler(router).listen(0).await().actualPort()
    }
}