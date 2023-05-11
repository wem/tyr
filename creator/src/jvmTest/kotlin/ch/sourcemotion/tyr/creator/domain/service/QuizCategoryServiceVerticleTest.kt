package ch.sourcemotion.tyr.creator.domain.service

import ch.sourcemotion.tyr.creator.datamapping.toDto
import ch.sourcemotion.tyr.creator.datamapping.toDtos
import ch.sourcemotion.tyr.creator.domain.repository.QuizCategoryRepository
import ch.sourcemotion.tyr.creator.domain.repository.QuizCategoryRepositoryException
import ch.sourcemotion.tyr.creator.domain.service.QuizCategoryService.*
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

class QuizCategoryServiceVerticleTest : AbstractServiceVerticleTest() {

    private lateinit var sutClient: QuizCategoryService

    @BeforeEach
    fun setUp() {
        sutClient = QuizCategoryService.create(vertx)
    }

    @Test
    fun `create or update - success`(testContext: VertxTestContext) = testContext.async(2) { checkpoint ->
        val quizStage = quizJune.stages.first()
        val quizCategoryToCreateOrUpdate = quizStage.categories.first()

        vertx.shareFactory {
            mockk<QuizCategoryRepository> {
                coEvery { save(any(), any()) } answers { call ->
                    testContext.verify {
                        call.invocation.args[1].asClue {
                            it.shouldBe(quizCategoryToCreateOrUpdate)
                        }
                    }
                    checkpoint.flag()
                }
            }
        }

        deploySut()

        sutClient.createOrUpdateQuizCategory(
            CreateOrUpdateQuizCategoryCmd(
                quizStage.id,
                quizCategoryToCreateOrUpdate.toDto()
            )
        )
        sutClient.createOrUpdateQuizCategory(
            CreateOrUpdateQuizCategoryCmd(
                quizStage.id,
                quizCategoryToCreateOrUpdate.toDto()
            )
        )
    }

    @Test
    fun `create or update - repo failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val quizStage = quizJune.stages.first()
        val quizCategoryToCreateOrUpdate = quizStage.categories.first()

        vertx.shareFactory {
            mockk<QuizCategoryRepository> {
                coEvery { save(any(), any()) } answers { call ->
                    testContext.verify {
                        call.invocation.args[1].asClue {
                            it.shouldBe(quizCategoryToCreateOrUpdate)
                        }
                    }
                    checkpoint.flag()
                    throw QuizCategoryRepositoryException("test failure")
                }
            }
        }

        deploySut()

        shouldThrow<QuizCategoryServiceException> {
            sutClient.createOrUpdateQuizCategory(
                CreateOrUpdateQuizCategoryCmd(
                    quizStage.id,
                    quizCategoryToCreateOrUpdate.toDto()
                )
            )
        }.cause.shouldBeInstanceOf<QuizCategoryRepositoryException>()
    }

    @Test
    fun `get quiz category - success`(testContext: VertxTestContext) = testContext.async {
        val quizCategoryToGet = quizJune.stages.first().categories.first()

        vertx.shareFactory {
            mockk<QuizCategoryRepository> {
                coEvery { findById(quizCategoryToGet.id) } returns quizCategoryToGet
            }
        }

        deploySut()

        sutClient.getQuizCategory(GetQuizCategoryQuery(quizCategoryToGet.id))
            .shouldBe(quizCategoryToGet.toDto())
    }

    @Test
    fun `get quiz category - repo failure`(testContext: VertxTestContext) = testContext.async {
        val quizCategoryToGet = quizJune.stages.first().categories.first()

        vertx.shareFactory {
            mockk<QuizCategoryRepository> {
                coEvery { findById(quizCategoryToGet.id) } throws QuizCategoryRepositoryException("test failure")
            }
        }

        deploySut()

        shouldThrow<QuizCategoryServiceException> {
            sutClient.getQuizCategory(GetQuizCategoryQuery(quizCategoryToGet.id))
                .shouldBe(quizCategoryToGet.toDto())
        }.cause.shouldBeInstanceOf<QuizCategoryRepositoryException>()
    }

    @Test
    fun `get quiz categories - success`(testContext: VertxTestContext) = testContext.async {
        val quizStage = quizJune.stages.first()
        val quizCategoriesToGet = quizStage.categories

        vertx.shareFactory {
            mockk<QuizCategoryRepository> {
                coEvery { findAllOfStage(quizStage.id) } returns quizCategoriesToGet
            }
        }

        deploySut()

        sutClient.getQuizCategories(GetQuizCategoriesQuery(quizStage.id))
            .shouldBe(quizCategoriesToGet.toDtos())
    }

    @Test
    fun `get quiz categories - repo failure`(testContext: VertxTestContext) = testContext.async {
        val quizStage = quizJune.stages.first()

        vertx.shareFactory {
            mockk<QuizCategoryRepository> {
                coEvery { findAllOfStage(quizStage.id) } throws QuizCategoryRepositoryException("test failure")
            }
        }

        deploySut()

        shouldThrow<QuizCategoryServiceException> {
            sutClient.getQuizCategories(GetQuizCategoriesQuery(quizStage.id))
        }.cause.shouldBeInstanceOf<QuizCategoryRepositoryException>()
    }

    @Test
    fun `delete quiz category - success`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val quizCategoryToDelete = quizJune.stages.first().categories.first().id

        vertx.shareFactory {
            mockk<QuizCategoryRepository> {
                coEvery { delete(quizCategoryToDelete) } answers {
                    checkpoint.flag()
                }
            }
        }

        deploySut()

        sutClient.deleteQuizCategory(DeleteQuizCategoryCmd(quizCategoryToDelete))
    }

    @Test
    fun `delete quiz category - repo failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val quizCategoryToDelete = quizJune.stages.first().categories.first().id

        vertx.shareFactory {
            mockk<QuizCategoryRepository> {
                coEvery { delete(quizCategoryToDelete) } answers {
                    checkpoint.flag()
                    throw QuizCategoryRepositoryException("test failure")
                }
            }
        }

        deploySut()

        shouldThrow<QuizCategoryServiceException> {
            sutClient.deleteQuizCategory(DeleteQuizCategoryCmd(quizCategoryToDelete))
        }.cause.shouldBeInstanceOf<QuizCategoryRepositoryException>()
    }

    private suspend fun deploySut() {
        vertx.deployVerticle(QuizCategoryServiceVerticle::class.java, deploymentOptionsOf()).await()
    }
}