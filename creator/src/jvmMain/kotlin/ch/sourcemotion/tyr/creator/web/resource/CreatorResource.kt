package ch.sourcemotion.tyr.creator.web.resource

import io.vertx.core.Vertx
import io.vertx.ext.web.Router

class CreatorResource(private val vertx: Vertx, private val parentRouter: Router) {
    init {
        val resourceRouter = Router.router(vertx)
    }
}