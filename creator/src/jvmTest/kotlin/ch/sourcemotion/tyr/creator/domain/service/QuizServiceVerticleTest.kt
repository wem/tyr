package ch.sourcemotion.tyr.creator.domain.service

import ch.sourcemotion.tyr.creator.datamapping.toDto
import ch.sourcemotion.tyr.creator.datamapping.toDtos
import ch.sourcemotion.tyr.creator.domain.repository.QuizRepository
import ch.sourcemotion.tyr.creator.domain.repository.QuizRepositoryException
import ch.sourcemotion.tyr.creator.domain.service.QuizService.*
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

class QuizServiceVerticleTest : AbstractServiceVerticleTest() {

    private lateinit var sutClient: QuizService

    @BeforeEach
    fun setUp() {
        sutClient = QuizService.create(vertx)
    }

    @Test
    fun `create or update - success`(testContext: VertxTestContext) = testContext.async(2) { checkpoint ->
        val quizToCreateOrUpdate = quizJune

        vertx.shareFactory {
            mockk<QuizRepository> {
                coEvery { save(any()) } answers { call ->
                    testContext.verify {
                        call.invocation.args.first().asClue {
                            it.shouldBe(quizToCreateOrUpdate)
                        }
                    }
                    checkpoint.flag()
                }
            }
        }

        deploySut()

        sutClient.createOrUpdateQuiz(CreateOrUpdateQuizCmd(quizToCreateOrUpdate.toDto()))
        sutClient.createOrUpdateQuiz(CreateOrUpdateQuizCmd(quizToCreateOrUpdate.toDto()))
    }

    @Test
    fun `create or update - repo failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val quizToCreateOrUpdate = quizJune

        vertx.shareFactory {
            mockk<QuizRepository> {
                coEvery { save(any()) } answers { call ->
                    testContext.verify {
                        call.invocation.args.first().asClue {
                            it.shouldBe(quizToCreateOrUpdate)
                        }
                    }
                    checkpoint.flag()
                    throw QuizRepositoryException("test failure")
                }
            }
        }

        deploySut()

        shouldThrow<QuizServiceException> {
            sutClient.createOrUpdateQuiz(CreateOrUpdateQuizCmd(quizToCreateOrUpdate.toDto()))
        }.cause.shouldBeInstanceOf<QuizRepositoryException>()
    }

    @Test
    fun `get quiz - success`(testContext: VertxTestContext) = testContext.async {
        val quizToGet = quizJune

        vertx.shareFactory {
            mockk<QuizRepository> {
                coEvery { findById(quizToGet.id) } returns quizToGet
            }
        }

        deploySut()

        sutClient.getQuiz(GetQuizQuery(quizToGet.id, withStages = false, withCategories = false))
            .shouldBe(quizToGet.toDto())
    }

    @Test
    fun `get quiz - repo failure`(testContext: VertxTestContext) = testContext.async {
        val quizToGet = quizJune

        vertx.shareFactory {
            mockk<QuizRepository> {
                coEvery { findById(quizToGet.id) } throws QuizRepositoryException("test failure")
            }
        }

        deploySut()

        shouldThrow<QuizServiceException> {
            sutClient.getQuiz(GetQuizQuery(quizToGet.id, withStages = false, withCategories = false))
                .shouldBe(quizToGet.toDto())
        }.cause.shouldBeInstanceOf<QuizRepositoryException>()
    }

    @Test
    fun `get quizzes - success`(testContext: VertxTestContext) = testContext.async {
        val quizzesToGet = listOf(quizJune, quizJuly)

        vertx.shareFactory {
            mockk<QuizRepository> {
                coEvery { findAll() } returns quizzesToGet
            }
        }

        deploySut()

        sutClient.getQuizzes(GetQuizzesQuery(withStages = true, withCategories = true))
            .shouldBe(quizzesToGet.toDtos())
    }

    @Test
    fun `get quizzes - repo failure`(testContext: VertxTestContext) = testContext.async {
        vertx.shareFactory {
            mockk<QuizRepository> {
                coEvery { findAll() } throws QuizRepositoryException("test failure")
            }
        }

        deploySut()

        shouldThrow<QuizServiceException> {
            sutClient.getQuizzes(GetQuizzesQuery(withStages = false, withCategories = false))
        }.cause.shouldBeInstanceOf<QuizRepositoryException>()
    }

    @Test
    fun `delete quiz - success`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val quizIdToDelete = quizJune.id

        vertx.shareFactory {
            mockk<QuizRepository> {
                coEvery { delete(quizIdToDelete) } answers {
                    checkpoint.flag()
                }
            }
        }

        deploySut()

        sutClient.deleteQuiz(DeleteQuizCmd(quizIdToDelete))
    }

    @Test
    fun `delete quiz stage - repo failure`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val quizIdToDelete = quizJune.id

        vertx.shareFactory {
            mockk<QuizRepository> {
                coEvery { delete(quizIdToDelete) } answers {
                    checkpoint.flag()
                    throw QuizRepositoryException("test failure")
                }
            }
        }

        deploySut()

        shouldThrow<QuizServiceException> {
            sutClient.deleteQuiz(DeleteQuizCmd(quizIdToDelete))
        }.cause.shouldBeInstanceOf<QuizRepositoryException>()
    }

    private suspend fun deploySut() {
        vertx.deployVerticle(QuizServiceVerticle::class.java, deploymentOptionsOf()).await()
    }
}