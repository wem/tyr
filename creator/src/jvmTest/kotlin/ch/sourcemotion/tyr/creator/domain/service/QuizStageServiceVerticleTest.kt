package ch.sourcemotion.tyr.creator.domain.service

import ch.sourcemotion.tyr.creator.datamapping.toDto
import ch.sourcemotion.tyr.creator.datamapping.toDtos
import ch.sourcemotion.tyr.creator.domain.repository.QuizStageRepository
import ch.sourcemotion.tyr.creator.domain.repository.QuizStageRepositoryException
import ch.sourcemotion.tyr.creator.domain.service.QuizStageService.*
import ch.sourcemotion.tyr.creator.ext.shareFactory
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.coroutines.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QuizStageServiceVerticleTest : AbstractServiceVerticleTest() {

    private lateinit var sutClient: QuizStageService

    @BeforeEach
    fun setUp() {
        sutClient = QuizStageService.create(vertx)
    }

    @Test
    fun `create or update - success`(testContext: VertxTestContext) = testContext.async(2) { checkpoint ->
        val quizStageToCreateOrUpdate = quizJune.stages.first()

        vertx.shareFactory {
            mockk<QuizStageRepository> {
                coEvery { save(any(), any()) } answers { call ->
                    testContext.verify {
                        call.invocation.args[1].asClue {
                            it.shouldBe(quizStageToCreateOrUpdate)
                        }
                    }
                    checkpoint.flag()
                }
            }
        }

        deploySut()

        sutClient.createOrUpdateQuizStage(CreateOrUpdateQuizStageCmd(quizJune.id, quizStageToCreateOrUpdate.toDto()))
        sutClient.createOrUpdateQuizStage(CreateOrUpdateQuizStageCmd(quizJune.id, quizStageToCreateOrUpdate.toDto()))
    }

    @Test
    fun `create or update - repo failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val quizStageToCreateOrUpdate = quizJune.stages.first()

        vertx.shareFactory {
            mockk<QuizStageRepository> {
                coEvery { save(any(), any()) } answers { call ->
                    testContext.verify {
                        call.invocation.args[1].asClue {
                            it.shouldBe(quizStageToCreateOrUpdate)
                        }
                    }
                    checkpoint.flag()
                    throw QuizStageRepositoryException("test failure")
                }
            }
        }

        deploySut()

        shouldThrow<QuizStageServiceException> {
            sutClient.createOrUpdateQuizStage(
                CreateOrUpdateQuizStageCmd(
                    quizJune.id,
                    quizStageToCreateOrUpdate.toDto()
                )
            )
        }.cause.shouldBeInstanceOf<QuizStageRepositoryException>()
    }

    @Test
    fun `get quiz stage - success`(testContext: VertxTestContext) = testContext.async {
        val quizStageToGet = quizJuly.stages.first()

        val findByIdCheckpoint = testContext.checkpoint()
        val findByIdWithCategoriesCheckpoint = testContext.checkpoint()

        vertx.shareFactory {
            mockk<QuizStageRepository> {
                coEvery { findById(quizStageToGet.id) } answers  {
                    findByIdCheckpoint.flag()
                    quizStageToGet
                }
                coEvery { findByIdWithCategories(quizStageToGet.id) } answers {
                    findByIdWithCategoriesCheckpoint.flag()
                    quizStageToGet
                }
            }
        }

        deploySut()

        sutClient.getQuizStage(GetQuizStageQuery(quizStageToGet.id, false)).shouldBe(quizStageToGet.toDto())
        sutClient.getQuizStage(GetQuizStageQuery(quizStageToGet.id, true)).shouldBe(quizStageToGet.toDto())
    }

    @Test
    fun `get quiz stage - repo failure`(testContext: VertxTestContext) = testContext.async {
        val quizStageToGet = quizJuly.stages.first()

        vertx.shareFactory {
            mockk<QuizStageRepository> {
                coEvery { findById(quizStageToGet.id) } throws QuizStageRepositoryException("test failure")
                coEvery { findByIdWithCategories(quizStageToGet.id) } throws QuizStageRepositoryException("test failure")
            }
        }

        deploySut()

        shouldThrow<QuizStageServiceException> {
            sutClient.getQuizStage(GetQuizStageQuery(quizStageToGet.id, true)).shouldBe(quizStageToGet.toDto())
        }.cause.shouldBeInstanceOf<QuizStageRepositoryException>()

        shouldThrow<QuizStageServiceException> {
            sutClient.getQuizStage(GetQuizStageQuery(quizStageToGet.id, false)).shouldBe(quizStageToGet.toDto())
        }.cause.shouldBeInstanceOf<QuizStageRepositoryException>()
    }

    @Test
    fun `get quiz stages - success`(testContext: VertxTestContext) = testContext.async {
        val quizStagesToGet = quizJune.stages

        vertx.shareFactory {
            mockk<QuizStageRepository> {
                coEvery { findAllOfQuiz(quizJune.id) } returns quizStagesToGet
            }
        }

        deploySut()

        sutClient.getQuizStages(GetQuizStagesQuery(quizJune.id, true)).shouldBe(quizStagesToGet.toDtos())
    }

    @Test
    fun `get quiz stages - repo failure`(testContext: VertxTestContext) = testContext.async {
        vertx.shareFactory {
            mockk<QuizStageRepository> {
                coEvery { findAllOfQuiz(quizJune.id) } throws QuizStageRepositoryException("test failure")
            }
        }

        deploySut()

        shouldThrow<QuizStageServiceException> {
            sutClient.getQuizStages(GetQuizStagesQuery(quizJune.id, true))
        }.cause.shouldBeInstanceOf<QuizStageRepositoryException>()
    }

    @Test
    fun `delete quiz stage - success`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val quizStageIdToDelete = quizJune.stages.first().id

        vertx.shareFactory {
            mockk<QuizStageRepository> {
                coEvery { delete(quizStageIdToDelete) } answers {
                    checkpoint.flag()
                }
            }
        }

        deploySut()

        sutClient.deleteQuizStage(DeleteQuizStageCmd(quizStageIdToDelete))
    }

    @Test
    fun `delete quiz stage - repo failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val quizStageIdToDelete = quizJune.stages.first().id

        vertx.shareFactory {
            mockk<QuizStageRepository> {
                coEvery { delete(quizStageIdToDelete) } answers {
                    checkpoint.flag()
                    throw QuizStageRepositoryException("test failure")
                }
            }
        }

        deploySut()

        shouldThrow<QuizStageServiceException> {
            sutClient.deleteQuizStage(DeleteQuizStageCmd(quizStageIdToDelete))
        }.cause.shouldBeInstanceOf<QuizStageRepositoryException>()
    }

    private suspend fun deploySut() {
        vertx.deployVerticle(QuizStageServiceVerticle::class.java, deploymentOptionsOf()).await()
    }
}