package ch.sourcemotion.tyr.creator.web.resource

import ch.sourcemotion.tyr.creator.domain.service.QuizCategoryService.*
import ch.sourcemotion.tyr.creator.domain.service.QuizStageService.GetQuizStageQuery
import ch.sourcemotion.tyr.creator.dto.QuizCategoryDto
import ch.sourcemotion.tyr.creator.dto.element.TextDto.Companion.textDtoOf
import ch.sourcemotion.tyr.creator.dto.jsonSerialization
import ch.sourcemotion.tyr.creator.dto.question.SimpleElementQuestionDto
import ch.sourcemotion.tyr.creator.ext.ack
import ch.sourcemotion.tyr.creator.ext.newUUID
import ch.sourcemotion.tyr.creator.ext.toUtf8String
import ch.sourcemotion.tyr.creator.testing.AbstractVertxTest
import ch.sourcemotion.tyr.creator.testing.VertxWebTest
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.netty.handler.codec.http.HttpHeaderNames.ACCEPT
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE
import io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.WebClient
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import io.vertx.uritemplate.UriTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QuizCategoryResourceTest : AbstractVertxTest(), VertxWebTest {

    private companion object {
        val json = jsonSerialization()

        const val LOCAL_HOST = "localhost"

        const val QUIZ_STAGE_ID_PATH_PARAM = "quizStageId"
        const val QUIZ_CATEGORY_ID_PATH_PARAM = "quizCategoryId"

        val getAllAndPutQuizCategoriesTemplate: UriTemplate =
            UriTemplate.of("/creator/stages/{$QUIZ_STAGE_ID_PATH_PARAM}/categories")

        val getAndDeleteQuizCategoryTemplate: UriTemplate =
            UriTemplate.of("/creator/categories/{$QUIZ_CATEGORY_ID_PATH_PARAM}")
    }


    private lateinit var client: WebClient
    private val quizStageId = newUUID()
    private val quizCategoryId = newUUID()

    @BeforeEach
    fun setUp() {
        client = WebClient.create(vertx)
    }

    @Test
    fun `put quiz category - successful`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        val quizCategoryDto = QuizCategoryDto(quizCategoryId, "Title", 1, textDtoOf("Context"))

        vertx.eventBus().consumer<CreateOrUpdateQuizCategoryCmd>(CreateOrUpdateQuizCategoryCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd ->
                    cmd.quizStageId.shouldBe(quizStageId)
                    cmd.quizCategoryDto.shouldBe(quizCategoryDto)
                }
            }
            checkpoint.flag()
            msg.ack()
        }

        val response = client.put(serverPort, LOCAL_HOST, getAllAndPutQuizCategoriesTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "$quizStageId")
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer(json.encodeToString(quizCategoryDto)))
            .await()

        response.statusCode().shouldBe(OK.code())
    }

    @Test
    fun `put quiz category - invalid request values`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()
        val quizStageDto = QuizCategoryDto(quizCategoryId, "Title", 1, textDtoOf("Context"))

        vertx.eventBus().consumer<CreateOrUpdateQuizCategoryCmd>(CreateOrUpdateQuizCategoryCmd.address) { msg ->
            testContext.failNow("Should not get called")
        }

        // Invalid quiz stage id
        client.put(serverPort, LOCAL_HOST, getAllAndPutQuizCategoriesTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "sdlfjlsdfjlsdjlsjfljsldfjlsdjf")
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer(json.encodeToString(quizStageDto))).await()
            .statusCode().shouldBe(BAD_REQUEST.code())

        // Missing body
        client.put(serverPort, LOCAL_HOST, getAllAndPutQuizCategoriesTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "${newUUID()}")
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer()).await()
            .statusCode().shouldBe(BAD_REQUEST.code())

        // Invalid body
        client.put(serverPort, LOCAL_HOST, getAllAndPutQuizCategoriesTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "${newUUID()}")
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer("some invalid body")).await()
            .statusCode().shouldBe(BAD_REQUEST.code())
    }

    @Test
    fun `put quiz category - service failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        val quizCategoryDto = QuizCategoryDto(quizCategoryId, "Title", 1, textDtoOf("Context"))

        vertx.eventBus().consumer<CreateOrUpdateQuizCategoryCmd>(CreateOrUpdateQuizCategoryCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd ->
                    cmd.quizStageId.shouldBe(quizStageId)
                    cmd.quizCategoryDto.shouldBe(quizCategoryDto)
                }
            }
            checkpoint.flag()
            msg.reply(Exception("Service failure"))
        }

        client.put(serverPort, LOCAL_HOST, getAllAndPutQuizCategoriesTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "$quizStageId")
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer(json.encodeToString(quizCategoryDto)))
            .await().statusCode().shouldBe(INTERNAL_SERVER_ERROR.code())
    }

    @Test
    fun `get quiz categories - successful`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val quizCategoryDtos = (1..5).map { number ->
            QuizCategoryDto(
                newUUID(), "Category", number, textDtoOf("Context"), listOf(
                    SimpleElementQuestionDto(textDtoOf("Question"), textDtoOf("Answer"))
                )
            )
        }

        vertx.eventBus().consumer<GetQuizCategoriesQuery>(GetQuizCategoriesQuery.address) { msg ->
            testContext.verify {
                msg.body().asClue { query ->
                    query.quizStageId.shouldBe(quizStageId)
                }
            }

            msg.reply(quizCategoryDtos)
        }

        client.get(serverPort, LOCAL_HOST, getAllAndPutQuizCategoriesTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "$quizStageId")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(OK.code())
                val respondQuizCategoryDtos: List<QuizCategoryDto> =
                    json.decodeFromString(response.body().toUtf8String())
                respondQuizCategoryDtos.shouldBe(quizCategoryDtos)
            }
    }

    @Test
    fun `get quiz categories - service failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetQuizCategoriesQuery>(GetQuizCategoriesQuery.address) { msg ->
            checkpoint.flag()
            msg.reply(Exception("Service failure"))
        }

        client.get(serverPort, LOCAL_HOST, getAllAndPutQuizCategoriesTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "$quizStageId")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(INTERNAL_SERVER_ERROR.code())
            }
    }

    @Test
    fun `get quiz categories - invalid request values`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetQuizCategoriesQuery>(GetQuizCategoriesQuery.address) { msg ->
            testContext.failNow("Should not get called")
        }

        client.get(serverPort, LOCAL_HOST, getAllAndPutQuizCategoriesTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "sdflkjsdfkjsldkfj")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(BAD_REQUEST.code())
            }
    }

    @Test
    fun `get quiz category - successful`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val quizCategoryDto = QuizCategoryDto(
            newUUID(), "Category", 1, textDtoOf("Context"), listOf(
                SimpleElementQuestionDto(textDtoOf("Question"), textDtoOf("Answer"))
            )
        )

        vertx.eventBus().consumer<GetQuizCategoryQuery>(GetQuizCategoryQuery.address) { msg ->
            testContext.verify {
                msg.body().asClue { query ->
                    query.id.shouldBe(quizCategoryId)
                }
            }

            msg.reply(quizCategoryDto)
        }

        client.get(serverPort, LOCAL_HOST, getAndDeleteQuizCategoryTemplate)
            .setTemplateParam(QUIZ_CATEGORY_ID_PATH_PARAM, "$quizCategoryId")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(OK.code())
                val respondQuizCategoryDto: QuizCategoryDto = json.decodeFromString(response.body().toUtf8String())
                respondQuizCategoryDto.shouldBe(quizCategoryDto)
            }
    }

    @Test
    fun `get quiz category - service failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetQuizCategoryQuery>(GetQuizCategoryQuery.address) { msg ->
            checkpoint.flag()
            msg.reply(Exception("Service failure"))
        }

        client.get(serverPort, LOCAL_HOST, getAndDeleteQuizCategoryTemplate)
            .setTemplateParam(QUIZ_CATEGORY_ID_PATH_PARAM, "$quizCategoryId")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(INTERNAL_SERVER_ERROR.code())
            }
    }

    @Test
    fun `get quiz category - not found`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetQuizCategoryQuery>(GetQuizCategoryQuery.address) { msg ->
            msg.reply(null)
        }

        client.get(serverPort, LOCAL_HOST, getAndDeleteQuizCategoryTemplate)
            .setTemplateParam(QUIZ_CATEGORY_ID_PATH_PARAM, "$quizCategoryId")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(NOT_FOUND.code())
            }
    }

    @Test
    fun `get quiz category - invalid request values`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetQuizStageQuery>(GetQuizStageQuery.address) { msg ->
            testContext.failNow("Should not get called")
        }

        // Invalid quiz category id
        client.get(serverPort, LOCAL_HOST, getAndDeleteQuizCategoryTemplate)
            .setTemplateParam(QUIZ_CATEGORY_ID_PATH_PARAM, "sdlfjslfjlsdfjlsjd")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(BAD_REQUEST.code())
            }
    }

    @Test
    fun `delete quiz category - successful`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<DeleteQuizCategoryCmd>(DeleteQuizCategoryCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd ->
                    cmd.id.shouldBe(quizCategoryId)
                }
            }
            msg.ack()
        }

        client.delete(serverPort, LOCAL_HOST, getAndDeleteQuizCategoryTemplate)
            .setTemplateParam(QUIZ_CATEGORY_ID_PATH_PARAM, "$quizCategoryId")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(OK.code())
            }
    }

    @Test
    fun `delete quiz category - service failure`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<DeleteQuizCategoryCmd>(DeleteQuizCategoryCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd ->
                    cmd.id.shouldBe(quizCategoryId)
                }
            }
            msg.reply(Exception("Service failure"))
        }

        client.delete(serverPort, LOCAL_HOST, getAndDeleteQuizCategoryTemplate)
            .setTemplateParam(QUIZ_CATEGORY_ID_PATH_PARAM, "$quizCategoryId")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(INTERNAL_SERVER_ERROR.code())
            }
    }

    private suspend fun CoroutineScope.deployCreatorContext(): Int {
        return withTestWebServer(vertx) {
            CreatorContext(vertx, this@deployCreatorContext).deploy(this)
        }
    }
}