package ch.sourcemotion.tyr.creator.web.resource

import ch.sourcemotion.tyr.creator.dto.jsonSerialization
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.serialization.json.Json

abstract class AbstractResource(
    protected val vertx: Vertx, protected val scope: CoroutineScope, protected val json: Json = jsonSerialization()
) : CoroutineScope by scope {

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
}