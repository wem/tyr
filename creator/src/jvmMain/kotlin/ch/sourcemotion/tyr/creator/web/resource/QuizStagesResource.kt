package ch.sourcemotion.tyr.creator.web.resource

import ch.sourcemotion.tyr.creator.domain.service.QuizStageService
import ch.sourcemotion.tyr.creator.domain.service.QuizStageService.*
import ch.sourcemotion.tyr.creator.dto.QuizStageDto
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

class QuizStagesResource(
    vertx: Vertx,
    scope: CoroutineScope,
    private val quizIdKey: String,
    json: Json
) : AbstractResource(vertx, scope, json) {

    private companion object : KLogging() {
        const val QUIZ_STAGE_ID_KEY = "quizStageId"
    }

    private val service = QuizStageService.create(vertx)

    override fun deploy(router: Router) {
        QuizCategoryResource(vertx, scope, QUIZ_STAGE_ID_KEY, json).deploy(router)

        router.put("/quizzes/:$quizIdKey/stages").handler(::onPutQuizStage)
            .consumes("${HttpHeaderValues.APPLICATION_JSON}")
        router.get("/quizzes/:$quizIdKey/stages").handler(::onGetQuizStages)
            .produces("${HttpHeaderValues.APPLICATION_JSON}")

        router.get("/stages/:$QUIZ_STAGE_ID_KEY").handler(::onGetQuizStage)
            .produces("${HttpHeaderValues.APPLICATION_JSON}")
        router.delete("/stages/:$QUIZ_STAGE_ID_KEY").handler(::onDeleteQuizStage)
            .produces("${HttpHeaderValues.APPLICATION_JSON}")
    }

    private fun onPutQuizStage(rc: RoutingContext) {
        runCatching {
            val quizId = UUID.fromString(rc.pathParam(quizIdKey))
            val quizStageDto = json.decodeFromString(QuizStageDto.serializer(), rc.body().asUtf8String())
            quizId to quizStageDto
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { (quizId, dto) ->
            rc.withExceptionHandling(mdcOf(quizId, dto.id)) {
                service.runCatching { createOrUpdateQuizStage(CreateOrUpdateQuizStageCmd(quizId, dto)) }
                    .onFailureAndRethrow { failure -> logger.error(failure) { "Failed to create / update quiz stage" } }
                rc.response().setStatusCode(HttpResponseStatus.OK.code()).end()
                logger.info { "Quiz stage created / updated" }
            }
        }
    }

    private fun onGetQuizStages(rc: RoutingContext) {
        runCatching {
            UUID.fromString(rc.pathParam(quizIdKey)) to
                    (rc.queryParam("withCategories").firstOrNull()?.toBoolean() ?: false)
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { (quizId, withCategories) ->
            rc.withExceptionHandling(mdcOf(quizId)) {
                val quizStageDtos = service.runCatching { getQuizStages(GetQuizStagesQuery(quizId, withCategories)) }
                    .onFailureAndRethrow { failure -> logger.error(failure) { "Failed to get quiz stages" } }
                rc.response()
                    .appJsonContentType()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .end(json.encodeToString(quizStageDtos))
            }
        }
    }

    private fun onGetQuizStage(rc: RoutingContext) {
        runCatching {
            val quizStageId: UUID = UUID.fromString(rc.pathParam(QUIZ_STAGE_ID_KEY))
            val withCategories = rc.queryParam("withCategories").firstOrNull()?.toBoolean() ?: false
            quizStageId to withCategories
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { (quizStageId, withCategories) ->
            rc.withExceptionHandling(mdcOf(quizStageId = quizStageId)) {
                val quizStageDto = service.runCatching { getQuizStage(GetQuizStageQuery(quizStageId, withCategories)) }
                    .onFailureAndRethrow { failure -> logger.error(failure) { "Failed to get quiz stage" } }
                if (quizStageDto != null) {
                    rc.response()
                        .appJsonContentType()
                        .setStatusCode(HttpResponseStatus.OK.code())
                        .end(json.encodeToString(quizStageDto))
                } else {
                    rc.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                        .setStatusMessage("Quiz stage for id '$quizStageId' not found")
                        .end()
                }
            }
        }
    }

    private fun onDeleteQuizStage(rc: RoutingContext) {
        runCatching {
            UUID.fromString(rc.pathParam(QUIZ_STAGE_ID_KEY))
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { quizStageId ->
            rc.withExceptionHandling(mdcOf(quizStageId = quizStageId)) {
                service.runCatching { deleteQuizStage(DeleteQuizStageCmd(quizStageId)) }
                    .onFailureAndRethrow { failure -> logger.error(failure) { "Failed to delete quiz stage" } }
                rc.response().setStatusCode(HttpResponseStatus.OK.code()).end()
                logger.info { "Quiz stage deleted" }
            }
        }
    }
}