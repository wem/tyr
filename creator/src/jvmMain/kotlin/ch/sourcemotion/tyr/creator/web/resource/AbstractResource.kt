package ch.sourcemotion.tyr.creator.web.resource

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.serialization.json.Json

abstract class AbstractResource(
    protected val vertx: Vertx, protected val scope: CoroutineScope, protected val json: Json
) : CoroutineScope by scope {

    abstract fun deploy(router: Router)

    protected inline fun RoutingContext.withExceptionHandling(
        mdc: MDCContext = MDCContext(),
        crossinline block: suspend RoutingContext.() -> Unit
    ) {
        launch(mdc) {
            try {
                block(this@withExceptionHandling)
            } catch (failure: Exception) {
                if (!response().ended()) {
                    fail(failure)
                }
            }
        }
    }

    protected fun HttpServerResponse.appJsonContentType() = apply {
        putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
    }
}