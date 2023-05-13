package ch.sourcemotion.tyr.creator.web.resource

import ch.sourcemotion.tyr.creator.domain.service.QuizService
import ch.sourcemotion.tyr.creator.domain.service.QuizStageService.*
import ch.sourcemotion.tyr.creator.dto.QuizCategoryDto
import ch.sourcemotion.tyr.creator.dto.QuizStageDto
import ch.sourcemotion.tyr.creator.dto.element.TextDto.Companion.textDtoOf
import ch.sourcemotion.tyr.creator.dto.jsonDtoSerialization
import ch.sourcemotion.tyr.creator.dto.question.SimpleElementQuestionDto
import ch.sourcemotion.tyr.creator.ext.ack
import ch.sourcemotion.tyr.creator.ext.newUUID
import ch.sourcemotion.tyr.creator.ext.toUtf8String
import ch.sourcemotion.tyr.creator.testing.AbstractVertxTest
import ch.sourcemotion.tyr.creator.testing.VertxWebTest
import io.kotest.assertions.asClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
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
import java.util.*

class QuizStagesResourceTest : AbstractVertxTest(), VertxWebTest {

    private companion object {
        val json = jsonDtoSerialization()

        const val LOCAL_HOST = "localhost"

        const val QUIZ_ID_PATH_PARAM = "quizId"
        const val QUIZ_STAGE_ID_PATH_PARAM = "quizStageId"

        const val WITH_CATEGORIES_PATH_PARAM = "withCategories"

        val putQuizStageTemplate: UriTemplate =
            UriTemplate.of("/creator/quizzes/{$QUIZ_ID_PATH_PARAM}/stages")
        val getQuizStagesTemplate: UriTemplate =
            UriTemplate.of("/creator/quizzes/{$QUIZ_ID_PATH_PARAM}/stages?withCategories={$WITH_CATEGORIES_PATH_PARAM}")

        val getQuizStageTemplate: UriTemplate =
            UriTemplate.of("/creator/stages/{$QUIZ_STAGE_ID_PATH_PARAM}?withCategories={$WITH_CATEGORIES_PATH_PARAM}")
        val deleteQuizStageTemplate: UriTemplate =
            UriTemplate.of("/creator/stages/{$QUIZ_STAGE_ID_PATH_PARAM}")
    }


    private lateinit var client: WebClient
    private val quizId = newUUID()
    private val quizStageId = newUUID()

    @BeforeEach
    fun setUp() {
        client = WebClient.create(vertx)
    }


    @Test
    fun `put quiz stage - successful`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        val quizStageDto = QuizStageDto(quizStageId, 1, "description")

        vertx.eventBus().consumer<CreateOrUpdateQuizStageCmd>(CreateOrUpdateQuizStageCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd ->
                    cmd.quizId.shouldBe(quizId)
                    cmd.quizStageDto.shouldBe(quizStageDto)
                }
            }
            checkpoint.flag()
            msg.ack()
        }

        val response = client.put(serverPort, LOCAL_HOST, putQuizStageTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "$quizId")
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer(json.encodeToString(quizStageDto)))
            .await()

        response.statusCode().shouldBe(OK.code())
    }

    @Test
    fun `put quiz stage - invalid request values`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()
        val quizStageDto = QuizStageDto(UUID.randomUUID(), 1, "description")

        vertx.eventBus().consumer<QuizService.CreateOrUpdateQuizCmd>(QuizService.CreateOrUpdateQuizCmd.address) { msg ->
            testContext.failNow("Should not get called")
        }

        // Invalid quiz id
        client.put(serverPort, LOCAL_HOST, putQuizStageTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "sdlfjlsdfjlsdjlsjfljsldfjlsdjf")
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer(json.encodeToString(quizStageDto))).await()
            .statusCode().shouldBe(BAD_REQUEST.code())

        // Missing body
        client.put(serverPort, LOCAL_HOST, putQuizStageTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "${newUUID()}")
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer()).await()
            .statusCode().shouldBe(BAD_REQUEST.code())

        // Invalid body
        client.put(serverPort, LOCAL_HOST, putQuizStageTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "${newUUID()}")
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer("some invalid body")).await()
            .statusCode().shouldBe(BAD_REQUEST.code())
    }

    @Test
    fun `put quiz stage - service failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        val quizStageDto = QuizStageDto(quizStageId, 1, "description")

        vertx.eventBus().consumer<CreateOrUpdateQuizStageCmd>(CreateOrUpdateQuizStageCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd ->
                    cmd.quizId.shouldBe(quizId)
                    cmd.quizStageDto.shouldBe(quizStageDto)
                }
            }
            checkpoint.flag()
            msg.reply(Exception("Service failure"))
        }

        client.put(serverPort, LOCAL_HOST, putQuizStageTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "$quizId")
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer(json.encodeToString(quizStageDto)))
            .await().statusCode().shouldBe(INTERNAL_SERVER_ERROR.code())
    }

    @Test
    fun `get stages - successful full path`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val quizStages = (1..5).map { number ->
            QuizStageDto(
                newUUID(), number, "description", listOf(
                    QuizCategoryDto(
                        newUUID(), "Category", 1, textDtoOf("Context"), listOf(
                            SimpleElementQuestionDto(textDtoOf("Question"), textDtoOf("Answer"))
                        )
                    )
                )
            )
        }

        vertx.eventBus().consumer<GetQuizStagesQuery>(GetQuizStagesQuery.address) { msg ->
            testContext.verify {
                msg.body().asClue { query -> query.withCategories.shouldBeTrue() }
            }

            msg.reply(quizStages)
        }

        client.get(serverPort, LOCAL_HOST, getQuizStagesTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "$quizId")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "true")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(OK.code())
                val respondQuizStages: List<QuizStageDto> = json.decodeFromString(response.body().toUtf8String())
                respondQuizStages.shouldBe(quizStages)
            }
    }

    @Test
    fun `get stages - successful only stages`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val quizStages = (1..5).map { number ->
            QuizStageDto(newUUID(), number, "description")
        }

        vertx.eventBus().consumer<GetQuizStagesQuery>(GetQuizStagesQuery.address) { msg ->
            testContext.verify {
                msg.body().asClue { query ->
                    query.withCategories.shouldBeFalse()
                }
            }

            msg.reply(quizStages)
        }

        client.get(serverPort, LOCAL_HOST, getQuizStagesTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "$quizId")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(OK.code())
                val respondQuizStages: List<QuizStageDto> = json.decodeFromString(response.body().toUtf8String())
                respondQuizStages.shouldBe(quizStages)
            }
    }

    @Test
    fun `get stages - service failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetQuizStagesQuery>(GetQuizStagesQuery.address) { msg ->
            checkpoint.flag()
            msg.reply(Exception("Service failure"))
        }

        client.get(serverPort, LOCAL_HOST, getQuizStagesTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "$quizId")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(INTERNAL_SERVER_ERROR.code())
            }
    }

    @Test
    fun `get stage - successful full path`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val quizStage = QuizStageDto(
            quizStageId, 1, "description", listOf(
                QuizCategoryDto(
                    newUUID(), "Category", 1, textDtoOf("Context"), listOf(
                        SimpleElementQuestionDto(textDtoOf("Question"), textDtoOf("Answer"))
                    )
                )
            )
        )

        vertx.eventBus().consumer<GetQuizStageQuery>(GetQuizStageQuery.address) { msg ->
            testContext.verify {
                msg.body().asClue { query ->
                    query.withCategories.shouldBeTrue()
                }
            }

            msg.reply(quizStage)
        }

        client.get(serverPort, LOCAL_HOST, getQuizStageTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "$quizStageId")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "true")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(OK.code())
                val respondQuizStage: QuizStageDto = json.decodeFromString(response.body().toUtf8String())
                respondQuizStage.shouldBe(quizStage)
            }
    }

    @Test
    fun `get stage - successful only stage`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val quizStage = QuizStageDto(quizStageId, 1, "description", listOf())

        vertx.eventBus().consumer<GetQuizStageQuery>(GetQuizStageQuery.address) { msg ->
            testContext.verify {
                msg.body().asClue { query ->
                    query.withCategories.shouldBeFalse()
                }
            }

            msg.reply(quizStage)
        }

        client.get(serverPort, LOCAL_HOST, getQuizStageTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "$quizStageId")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(OK.code())
                val respondQuizStage: QuizStageDto = json.decodeFromString(response.body().toUtf8String())
                respondQuizStage.shouldBe(quizStage)
            }
    }

    @Test
    fun `get stage - service failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetQuizStageQuery>(GetQuizStageQuery.address) { msg ->
            checkpoint.flag()
            msg.reply(Exception("Service failure"))
        }

        client.get(serverPort, LOCAL_HOST, getQuizStageTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "$quizStageId")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(INTERNAL_SERVER_ERROR.code())
            }
    }

    @Test
    fun `get stage - not found`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetQuizStageQuery>(GetQuizStageQuery.address) { msg ->
            msg.reply(null)
        }

        client.get(serverPort, LOCAL_HOST, getQuizStageTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "$quizStageId")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(NOT_FOUND.code())
            }
    }

    @Test
    fun `get stage - invalid request values`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetQuizStageQuery>(GetQuizStageQuery.address) { msg ->
            testContext.failNow("Should not get called")
        }

        client.get(serverPort, LOCAL_HOST, getQuizStageTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "sldfjslfkjsofijsolfmsldf")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(BAD_REQUEST.code())
            }
    }

    @Test
    fun `delete stage - successful`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<DeleteQuizStageCmd>(DeleteQuizStageCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd -> cmd.id.shouldBe(quizStageId) }
            }
            msg.ack()
        }

        client.delete(serverPort, LOCAL_HOST, deleteQuizStageTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "$quizStageId")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(OK.code())
            }
    }

    @Test
    fun `delete stage - service failure`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<DeleteQuizStageCmd>(DeleteQuizStageCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd -> cmd.id.shouldBe(quizStageId) }
            }
            msg.reply(Exception("Service failure"))
        }

        client.delete(serverPort, LOCAL_HOST, deleteQuizStageTemplate)
            .setTemplateParam(QUIZ_STAGE_ID_PATH_PARAM, "$quizStageId")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
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