package ch.sourcemotion.tyr.creator.web.resource

import ch.sourcemotion.tyr.creator.domain.service.QuizCategoryService
import ch.sourcemotion.tyr.creator.domain.service.QuizCategoryService.*
import ch.sourcemotion.tyr.creator.dto.QuizCategoryDto
import ch.sourcemotion.tyr.creator.dto.jsonDtoSerialization
import ch.sourcemotion.tyr.creator.ext.asUtf8String
import ch.sourcemotion.tyr.creator.ext.onFailureAndRethrow
import ch.sourcemotion.tyr.creator.logging.mdcOf
import ch.sourcemotion.tyr.creator.web.resource.exception.BadRequestException
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KLogging
import java.util.*

class QuizCategoryResource(
    vertx: Vertx,
    scope: CoroutineScope,
    private val quizStageIdKey: String,
    json: Json = jsonDtoSerialization()
) :
    AbstractResource(vertx, scope, json) {

    private companion object : KLogging() {
        const val QUIZ_CATEGORY_ID_KEY = "quizCategoryId"
    }

    private val service = QuizCategoryService.create(vertx)

    fun deploy(router: Router) {
        router.put("/stages/:$quizStageIdKey/categories").handler(::onPutQuizCategory)
            .consumes("${HttpHeaderValues.APPLICATION_JSON}")
        router.get("/stages/:$quizStageIdKey/categories").handler(::onGetQuizCategories)
            .produces("${HttpHeaderValues.APPLICATION_JSON}")

        router.get("/categories/:$QUIZ_CATEGORY_ID_KEY").handler(::onGetQuizCategory)
            .produces("${HttpHeaderValues.APPLICATION_JSON}")
        router.delete("/categories/:$QUIZ_CATEGORY_ID_KEY").handler(::onDeleteQuizCategory)
            .produces("${HttpHeaderValues.APPLICATION_JSON}")
    }

    private fun onPutQuizCategory(rc: RoutingContext) {
        runCatching {
            val quizStageId = UUID.fromString(rc.pathParam(quizStageIdKey))
            val quizCategoryDto = json.decodeFromString(QuizCategoryDto.serializer(), rc.body().asUtf8String())
            quizStageId to quizCategoryDto
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { (quizStageId, dto) ->
            rc.withExceptionHandling(mdcOf(quizCategoryId = dto.id, quizStageId = quizStageId)) {
                service.runCatching { createOrUpdateQuizCategory(CreateOrUpdateQuizCategoryCmd(quizStageId, dto)) }
                    .onFailureAndRethrow { failure ->
                        logger.error(failure) {
                            "Failed to create / update quiz category '${rc.body().asUtf8String()}'"
                        }
                    }
                rc.response().setStatusCode(OK.code()).end()
                logger.info { "Quiz category created / updated" }
            }
        }
    }

    private fun onGetQuizCategories(rc: RoutingContext) {
        runCatching {
            UUID.fromString(rc.pathParam(quizStageIdKey))
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { quizStageId ->
            rc.withExceptionHandling(mdcOf(quizStageId = quizStageId)) {
                val quizCategoryDtos = service.runCatching { getQuizCategories(GetQuizCategoriesQuery(quizStageId)) }
                    .onFailureAndRethrow { failure -> logger.error(failure) { "Failed to get quiz categories" } }
                rc.response()
                    .appJsonContentType()
                    .setStatusCode(OK.code())
                    .end(json.encodeToString(quizCategoryDtos))
            }
        }
    }

    private fun onGetQuizCategory(rc: RoutingContext) {
        runCatching {
            UUID.fromString(rc.pathParam(QUIZ_CATEGORY_ID_KEY))
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { quizCategoryId ->
            rc.withExceptionHandling(mdcOf(quizCategoryId = quizCategoryId)) {
                val quizCategoryDto = service.runCatching { getQuizCategory(GetQuizCategoryQuery(quizCategoryId)) }
                    .onFailureAndRethrow { logger.error { "Failed to get quiz category" } }
                if (quizCategoryDto != null) {
                    rc.response()
                        .appJsonContentType()
                        .setStatusCode(OK.code())
                        .end(json.encodeToString(quizCategoryDto))
                } else {
                    rc.response().setStatusCode(NOT_FOUND.code())
                        .setStatusMessage("Quiz category for id '$quizCategoryId' not found")
                        .end()
                }
            }
        }
    }

    private fun onDeleteQuizCategory(rc: RoutingContext) {
        runCatching {
            UUID.fromString(rc.pathParam(QUIZ_CATEGORY_ID_KEY))
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { quizCategoryId ->
            rc.withExceptionHandling(mdcOf(quizCategoryId = quizCategoryId)) {
                service.runCatching { deleteQuizCategory(DeleteQuizCategoryCmd(quizCategoryId)) }
                    .onFailureAndRethrow { failure -> logger.warn(failure) { "Failed to delete quiz category" } }
                rc.response().setStatusCode(OK.code()).end()
                logger.info { "Quiz category deleted" }
            }
        }
    }
}