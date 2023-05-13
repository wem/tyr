package ch.sourcemotion.tyr.creator.web.resource

import ch.sourcemotion.tyr.creator.domain.service.QuizService
import ch.sourcemotion.tyr.creator.domain.service.QuizService.*
import ch.sourcemotion.tyr.creator.dto.QuizDto
import ch.sourcemotion.tyr.creator.dto.jsonDtoSerialization
import ch.sourcemotion.tyr.creator.ext.asUtf8String
import ch.sourcemotion.tyr.creator.ext.onFailureAndRethrow
import ch.sourcemotion.tyr.creator.logging.mdcOf
import ch.sourcemotion.tyr.creator.web.resource.exception.BadRequestException
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KLogging
import java.util.*

class QuizzesResource(vertx: Vertx, scope: CoroutineScope, json: Json = jsonDtoSerialization()) :
    AbstractResource(vertx, scope, json) {

    private companion object : KLogging() {
        const val QUIZ_ID_KEY = "quizId"
    }

    private val service = QuizService.create(vertx)

    fun deploy(router: Router) {
        QuizStagesResource(vertx, scope, QUIZ_ID_KEY, json).deploy(router)

        router.put("/quizzes").handler(::onPutQuiz).consumes("${HttpHeaderValues.APPLICATION_JSON}")
        router.get("/quizzes").handler(::onGetQuizzes).produces("${HttpHeaderValues.APPLICATION_JSON}")

        router.get("/quizzes/:$QUIZ_ID_KEY").handler(::onGetQuiz).produces("${HttpHeaderValues.APPLICATION_JSON}")
        router.delete("/quizzes/:$QUIZ_ID_KEY").handler(::onDeleteQuiz).produces("${HttpHeaderValues.APPLICATION_JSON}")
    }

    private fun onPutQuiz(rc: RoutingContext) {
        runCatching {
            json.decodeFromString(QuizDto.serializer(), rc.body().asUtf8String())
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { dto ->
            rc.withExceptionHandling(mdcOf(dto.id)) {
                service.runCatching { createOrUpdateQuiz(CreateOrUpdateQuizCmd(dto)) }
                    .onFailureAndRethrow { failure -> logger.error(failure) { "Failed to create / update quiz" } }
                rc.response().setStatusCode(HttpResponseStatus.OK.code()).end()
                logger.info { "Quiz stage created / updated" }
            }
        }
    }

    private fun onGetQuizzes(rc: RoutingContext) {
        runCatching {
            (rc.queryParam("withStages").firstOrNull()?.toBoolean() ?: false) to
                    (rc.queryParam("withCategories").firstOrNull()?.toBoolean() ?: false)
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { (withStages, withCategories) ->
            rc.withExceptionHandling {
                val quizDtos = service.runCatching { getQuizzes(GetQuizzesQuery(withStages, withCategories)) }
                    .onFailureAndRethrow { failure -> logger.error(failure) { "Failed to get quizzes" } }
                rc.response()
                    .appJsonContentType()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .end(json.encodeToString(quizDtos))
            }
        }
    }

    private fun onGetQuiz(rc: RoutingContext) {
        runCatching {
            val quizId: UUID = UUID.fromString(rc.pathParam(QUIZ_ID_KEY))
            val withStages = rc.queryParam("withStages").firstOrNull()?.toBoolean() ?: false
            val withCategories = rc.queryParam("withCategories").firstOrNull()?.toBoolean() ?: false
            Triple(quizId, withStages, withCategories)
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { (quizId, withStages, withCategories) ->
            rc.withExceptionHandling(mdcOf(quizId)) {
                val quizDto = service.runCatching { getQuiz(GetQuizQuery(quizId, withStages, withCategories)) }
                    .onFailureAndRethrow { failure -> logger.error(failure) { "Failed to get quiz" } }
                if (quizDto != null) {
                    rc.response()
                        .appJsonContentType()
                        .setStatusCode(HttpResponseStatus.OK.code())
                        .end(json.encodeToString(QuizDto.serializer(), quizDto))
                } else {
                    rc.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                        .setStatusMessage("Quiz for id '$quizId' not found").end()
                }
            }
        }
    }

    private fun onDeleteQuiz(rc: RoutingContext) {
        runCatching {
            UUID.fromString(rc.pathParam(QUIZ_ID_KEY))
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { quizId ->
            rc.withExceptionHandling(mdcOf(quizId)) {
                service.runCatching { deleteQuiz(DeleteQuizCmd(quizId)) }
                    .onFailureAndRethrow { failure -> logger.warn(failure) { "Failed to delete quiz" } }
                rc.response().setStatusCode(HttpResponseStatus.OK.code()).end()
                logger.info { "Quiz deleted" }
            }
        }
    }
}