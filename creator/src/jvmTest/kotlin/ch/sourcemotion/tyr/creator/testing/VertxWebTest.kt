package ch.sourcemotion.tyr.creator.testing

import ch.sourcemotion.tyr.creator.ext.toUtf8String
import io.kotest.matchers.shouldBe
import io.netty.handler.codec.http.HttpHeaderNames.ACCEPT
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE
import io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON
import io.netty.handler.codec.http.HttpHeaderValues.MULTIPART_FORM_DATA
import io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

interface VertxWebTest {

    suspend fun withTestWebServer(vertx: Vertx, block: Router.() -> Unit): Int {
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create(true))
        block(router)
        val server = vertx.createHttpServer()
        return server.requestHandler(router).listen(0).await().actualPort()
    }
}

fun <T> HttpRequest<T>.producesMultiPartForm() = apply {
    putHeader("$CONTENT_TYPE", "$MULTIPART_FORM_DATA")
}

fun <T> HttpRequest<T>.producesJson() = apply {
    putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
}

fun <T> HttpRequest<T>.consumesJson() = apply {
    putHeader("$ACCEPT", "$APPLICATION_JSON")
}

fun <T> HttpResponse<T>.shouldBeOk() = apply { statusCode().shouldBe(OK.code()) }
fun <T> HttpResponse<T>.shouldBeNotFound() = apply { statusCode().shouldBe(NOT_FOUND.code()) }

inline fun <reified T : Any> HttpResponse<T>.bodyAs(json: Json) =
    json.decodeFromString<T>(bodyAsBuffer().toUtf8String())