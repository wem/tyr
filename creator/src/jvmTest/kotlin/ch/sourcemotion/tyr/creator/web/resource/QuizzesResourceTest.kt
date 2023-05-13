package ch.sourcemotion.tyr.creator.web.resource

import ch.sourcemotion.tyr.creator.domain.service.QuizService.*
import ch.sourcemotion.tyr.creator.dto.QuizCategoryDto
import ch.sourcemotion.tyr.creator.dto.QuizDto
import ch.sourcemotion.tyr.creator.dto.QuizStageDto
import ch.sourcemotion.tyr.creator.dto.element.TextDto
import ch.sourcemotion.tyr.creator.dto.jsonDtoSerialization
import ch.sourcemotion.tyr.creator.dto.question.SimpleElementQuestionDto
import ch.sourcemotion.tyr.creator.ext.ack
import ch.sourcemotion.tyr.creator.ext.newUUID
import ch.sourcemotion.tyr.creator.ext.toUtf8String
import ch.sourcemotion.tyr.creator.testing.AbstractVertxTest
import ch.sourcemotion.tyr.creator.testing.VertxWebTest
import com.benasher44.uuid.uuid4
import io.kotest.assertions.asClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.netty.handler.codec.http.HttpHeaderNames.ACCEPT
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE
import io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.WebClient
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import io.vertx.uritemplate.UriTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.decodeFromString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class QuizzesResourceTest : AbstractVertxTest(), VertxWebTest {

    private companion object {
        val json = jsonDtoSerialization()

        const val LOCAL_HOST = "localhost"

        const val QUIZ_ID_PATH_PARAM = "quizId"
        const val WITH_STAGES_PATH_PARAM = "withStages"
        const val WITH_CATEGORIES_PATH_PARAM = "withCategories"

        val putPutQuizzesTemplate: UriTemplate = UriTemplate.of("/creator/quizzes")
        val getQuizzesTemplate: UriTemplate =
            UriTemplate.of("/creator/quizzes?withStages={$WITH_STAGES_PATH_PARAM}&withCategories={$WITH_CATEGORIES_PATH_PARAM}")
        val getQuizTemplate: UriTemplate =
            UriTemplate.of("/creator/quizzes/{$QUIZ_ID_PATH_PARAM}?withStages={$WITH_STAGES_PATH_PARAM}&withCategories={$WITH_CATEGORIES_PATH_PARAM}")
        val deleteQuizTemplate: UriTemplate = UriTemplate.of("/creator/quizzes/{$QUIZ_ID_PATH_PARAM}")
    }

    private lateinit var client: WebClient

    private val quizId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        client = WebClient.create(vertx)
    }

    @Test
    fun `put quiz - successful`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        val quizId: UUID = UUID.randomUUID()
        val quizDto = QuizDto(quizId, LocalDate.now().toKotlinLocalDate())

        vertx.eventBus().consumer<CreateOrUpdateQuizCmd>(CreateOrUpdateQuizCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd ->
                    cmd.quizDto.date.shouldBe(quizDto.date)
                    cmd.quizDto.id.shouldBe(quizId)
                }
            }
            checkpoint.flag()
            msg.ack()
        }

        val response = client.put(serverPort, LOCAL_HOST, putPutQuizzesTemplate)
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer(json.encodeToString(QuizDto.serializer(), quizDto)))
            .await()

        response.statusCode().shouldBe(HttpResponseStatus.OK.code())
    }

    @Test
    fun `put quiz - invalid request values`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<CreateOrUpdateQuizCmd>(CreateOrUpdateQuizCmd.address) { msg ->
            testContext.failNow("Should not get called")
        }

        // Missing body
        client.put(serverPort, LOCAL_HOST, putPutQuizzesTemplate)
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer()).await()
            .statusCode().shouldBe(BAD_REQUEST.code())

        // Invalid body
        client.put(serverPort, LOCAL_HOST, putPutQuizzesTemplate)
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer("some invalid body")).await()
            .statusCode().shouldBe(BAD_REQUEST.code())
    }

    @Test
    fun `put quiz - service failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        val quizDto = QuizDto(quizId, LocalDate.now().toKotlinLocalDate())

        vertx.eventBus().consumer<CreateOrUpdateQuizCmd>(CreateOrUpdateQuizCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd ->
                    cmd.quizDto.date.shouldBe(quizDto.date)
                    cmd.quizDto.id.shouldBe(quizId)
                }
            }
            checkpoint.flag()
            msg.reply(Exception("Service failure"))
        }

        val response = client.put(serverPort, LOCAL_HOST, putPutQuizzesTemplate)
            .putHeader("$CONTENT_TYPE", "$APPLICATION_JSON")
            .sendBuffer(Buffer.buffer(json.encodeToString(QuizDto.serializer(), quizDto)))
            .await()

        response.statusCode().shouldBe(INTERNAL_SERVER_ERROR.code())
    }

    @Test
    fun `get quizzes - successful full path`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val quizDtos = (1..5).map {
            QuizDto(
                uuid4(), LocalDate.now().toKotlinLocalDate(), listOf(
                    QuizStageDto(
                        uuid4(), 1, "description", listOf(
                            QuizCategoryDto(
                                uuid4(), "Category title", 1, TextDto("some text", "some description"), listOf(
                                    SimpleElementQuestionDto(
                                        TextDto("Question", "Question description"),
                                        TextDto("Answer", "Answer description")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        }

        vertx.eventBus().consumer<GetQuizzesQuery>(GetQuizzesQuery.address) { msg ->
            testContext.verify {
                msg.body().asClue { query ->
                    query.withStages.shouldBeTrue()
                    query.withCategories.shouldBeTrue()
                }
            }

            msg.reply(quizDtos)
        }

        client.get(serverPort, LOCAL_HOST, getQuizzesTemplate)
            .setTemplateParam(WITH_STAGES_PATH_PARAM, "true")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "true")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(HttpResponseStatus.OK.code())
                val respondQuizzes: List<QuizDto> = json.decodeFromString(response.body().toUtf8String())
                respondQuizzes.shouldBe(quizDtos)
            }
    }

    @Test
    fun `get quizzes - successful only with stages`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val quizDtos = (1..5).map {
            QuizDto(
                newUUID(), LocalDate.now().toKotlinLocalDate(), listOf(
                    QuizStageDto(uuid4(), 1, "description", listOf())
                )
            )
        }

        vertx.eventBus().consumer<GetQuizzesQuery>(GetQuizzesQuery.address) { msg ->
            testContext.verify {
                msg.body().asClue { query ->
                    query.withStages.shouldBeTrue()
                    query.withCategories.shouldBeFalse()
                }
            }

            msg.reply(quizDtos)
        }

        client.get(serverPort, LOCAL_HOST, getQuizzesTemplate)
            .setTemplateParam(WITH_STAGES_PATH_PARAM, "true")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(HttpResponseStatus.OK.code())
                val respondQuizzes: List<QuizDto> = json.decodeFromString(response.body().toUtf8String())
                respondQuizzes.shouldBe(quizDtos)
            }
    }

    @Test
    fun `get quizzes - successful only quiz`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val quizDtos = (1..5).map {
            QuizDto(uuid4(), LocalDate.now().toKotlinLocalDate())
        }

        vertx.eventBus().consumer<GetQuizzesQuery>(GetQuizzesQuery.address) { msg ->
            testContext.verify {
                msg.body().asClue { query ->
                    query.withStages.shouldBeFalse()
                    query.withCategories.shouldBeFalse()
                }
            }

            msg.reply(quizDtos)
        }

        client.get(serverPort, LOCAL_HOST, getQuizzesTemplate)
            .setTemplateParam(WITH_STAGES_PATH_PARAM, "false")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(HttpResponseStatus.OK.code())
                val respondQuizzes: List<QuizDto> = json.decodeFromString(response.body().toUtf8String())
                respondQuizzes.shouldBe(quizDtos)
            }
    }

    @Test
    fun `get quizzes - service failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetQuizzesQuery>(GetQuizzesQuery.address) { msg ->
            checkpoint.flag()
            msg.reply(Exception("Service failure"))
        }

        client.get(serverPort, LOCAL_HOST, getQuizzesTemplate)
            .setTemplateParam(WITH_STAGES_PATH_PARAM, "false")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().statusCode().shouldBe(INTERNAL_SERVER_ERROR.code())
    }

    @Test
    fun `get quiz - successful full path`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val quizDto = QuizDto(
            uuid4(), LocalDate.now().toKotlinLocalDate(), listOf(
                QuizStageDto(
                    uuid4(), 1, "description", listOf(
                        QuizCategoryDto(
                            uuid4(), "Category title", 1, TextDto("some text", "some description"), listOf(
                                SimpleElementQuestionDto(
                                    TextDto("Question", "Question description"),
                                    TextDto("Answer", "Answer description")
                                )
                            )
                        )
                    )
                )
            )
        )

        vertx.eventBus().consumer<GetQuizQuery>(GetQuizQuery.address) { msg ->
            testContext.verify {
                msg.body().asClue { query ->
                    query.withStages.shouldBeTrue()
                    query.withCategories.shouldBeTrue()
                    query.id.shouldBe(quizDto.id)
                }
            }

            msg.reply(quizDto)
        }

        client.get(serverPort, LOCAL_HOST, getQuizTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "${quizDto.id}")
            .setTemplateParam(WITH_STAGES_PATH_PARAM, "true")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "true")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(HttpResponseStatus.OK.code())
                val respondQuiz: QuizDto = json.decodeFromString(response.body().toUtf8String())
                respondQuiz.shouldBe(quizDto)
            }
    }

    @Test
    fun `get quiz - successful only with stages`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val quizDto = QuizDto(
            uuid4(), LocalDate.now().toKotlinLocalDate(), listOf(
                QuizStageDto(uuid4(), 1, "description")
            )
        )

        vertx.eventBus().consumer<GetQuizQuery>(GetQuizQuery.address) { msg ->
            testContext.verify {
                msg.body().asClue { query ->
                    query.withStages.shouldBeTrue()
                    query.withCategories.shouldBeFalse()
                    query.id.shouldBe(quizDto.id)
                }
            }

            msg.reply(quizDto)
        }

        client.get(serverPort, LOCAL_HOST, getQuizTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "${quizDto.id}")
            .setTemplateParam(WITH_STAGES_PATH_PARAM, "true")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(HttpResponseStatus.OK.code())
                val respondQuiz: QuizDto = json.decodeFromString(response.body().toUtf8String())
                respondQuiz.shouldBe(quizDto)
            }
    }

    @Test
    fun `get quiz - successful only quiz`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val quizDto = QuizDto(uuid4(), LocalDate.now().toKotlinLocalDate())

        vertx.eventBus().consumer<GetQuizQuery>(GetQuizQuery.address) { msg ->
            testContext.verify {
                msg.body().asClue { query ->
                    query.withStages.shouldBeFalse()
                    query.withCategories.shouldBeFalse()
                    query.id.shouldBe(quizDto.id)
                }
            }

            msg.reply(quizDto)
        }

        client.get(serverPort, LOCAL_HOST, getQuizTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "${quizDto.id}")
            .setTemplateParam(WITH_STAGES_PATH_PARAM, "false")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().asClue { response ->
                response.statusCode().shouldBe(HttpResponseStatus.OK.code())
                val respondQuiz: QuizDto = json.decodeFromString(response.body().toUtf8String())
                respondQuiz.shouldBe(quizDto)
            }
    }

    @Test
    fun `get quiz - service failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetQuizQuery>(GetQuizQuery.address) { msg ->
            checkpoint.flag()
            msg.reply(Exception("Service failure"))
        }

        client.get(serverPort, LOCAL_HOST, getQuizTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "${UUID.randomUUID()}")
            .setTemplateParam(WITH_STAGES_PATH_PARAM, "false")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().statusCode().shouldBe(INTERNAL_SERVER_ERROR.code())
    }

    @Test
    fun `get quiz - not found`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetQuizQuery>(GetQuizQuery.address) { msg ->
            msg.reply(null)
        }

        client.get(serverPort, LOCAL_HOST, getQuizTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "${UUID.randomUUID()}")
            .setTemplateParam(WITH_STAGES_PATH_PARAM, "false")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().statusCode().shouldBe(HttpResponseStatus.NOT_FOUND.code())
    }

    @Test
    fun `get quiz - invalid quiz id`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetQuizQuery>(GetQuizQuery.address) { msg ->
            testContext.failNow("Should not get called")
        }

        client.get(serverPort, LOCAL_HOST, getQuizTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "lsdkjlkjlskjflksjlksdfo")
            .setTemplateParam(WITH_STAGES_PATH_PARAM, "false")
            .setTemplateParam(WITH_CATEGORIES_PATH_PARAM, "false")
            .putHeader("$ACCEPT", "$APPLICATION_JSON")
            .send()
            .await().statusCode().shouldBe(BAD_REQUEST.code())
    }

    @Test
    fun `delete quiz - successful`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<DeleteQuizCmd>(DeleteQuizCmd.address) { msg ->
            msg.ack()
        }

        client.delete(serverPort, LOCAL_HOST, deleteQuizTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "${newUUID()}")
            .send()
            .await().statusCode().shouldBe(HttpResponseStatus.OK.code())
    }

    @Test
    fun `delete quiz - service failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<DeleteQuizCmd>(DeleteQuizCmd.address) { msg ->
            checkpoint.flag()
            msg.reply(Exception("Service failure"))
        }

        client.delete(serverPort, LOCAL_HOST, deleteQuizTemplate)
            .setTemplateParam(QUIZ_ID_PATH_PARAM, "${newUUID()}")
            .send()
            .await().statusCode().shouldBe(INTERNAL_SERVER_ERROR.code())
    }


    private suspend fun CoroutineScope.deployCreatorContext(): Int {
        return withTestWebServer(vertx) {
            CreatorContext(vertx, this@deployCreatorContext).deploy(this)
        }
    }
}