package ch.sourcemotion.tyr.creator.web.resource

import ch.sourcemotion.tyr.creator.dto.jsonDtoSerialization
import ch.sourcemotion.tyr.creator.web.resource.exception.BadRequestException
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import mu.KLogging

class CreatorContext(
    private val vertx: Vertx,
    private val scope: CoroutineScope,
    private val json: Json = jsonDtoSerialization()
) : CoroutineScope by scope {

    private companion object : KLogging()

    fun deploy(parentRouter: Router) {
        val creatorRouter = Router.router(vertx)

        creatorRouter.route().failureHandler(this::onFailure)
        QuizzesResource(vertx, scope, json).deploy(creatorRouter)
        FileResource(vertx, scope, json).deploy(creatorRouter)

        parentRouter.route("/creator/*").subRouter(creatorRouter)
    }


    private fun onFailure(rc: RoutingContext) {
        when (val failure = rc.failure()) {
            is BadRequestException -> rc.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .setStatusMessage("Provided data missing or invalid")
                .end()

            else -> {
                logger.warn(failure) { "Server side failure" }
                rc.response()
                    .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                    .setStatusMessage("Something went wrong on server side")
                    .end()
            }
        }
    }
}