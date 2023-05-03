package ch.sourcemotion.tyr.creator.domain.repository

import ch.sourcemotion.tyr.creator.domain.entity.Quiz
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage
import ch.sourcemotion.tyr.creator.ext.getOrCreateByFactory
import ch.sourcemotion.tyr.creator.testing.AbstractVertxDatabaseTest
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class QuizRepositoryTest : AbstractVertxDatabaseTest() {

    private lateinit var categoryRepo: QuizCategoryRepository
    private lateinit var stageRepo: QuizStageRepository

    private lateinit var sut: QuizRepository

    @BeforeEach
    fun setUpSut() {
        categoryRepo = QuizCategoryRepository(vertx.getOrCreateByFactory())
        stageRepo = QuizStageRepository(vertx.getOrCreateByFactory())

        sut = QuizRepository(vertx.getOrCreateByFactory())
    }

    @Test
    fun `save find by id and delete`(testContext: VertxTestContext) = testContext.async {
        val quiz = Quiz.new(LocalDate.now())
        sut.save(quiz)
        sut.findById(quiz.id).shouldBe(quiz)
        sut.delete(quiz.id)
        sut.findById(quiz.id).shouldBeNull()
    }

    @Test
    fun `save and update`(testContext: VertxTestContext) = testContext.async {
        val quiz = Quiz.new(LocalDate.now())
        sut.save(quiz)
        val updatedQuiz = quiz.copy(date = quiz.date.plusDays(1))
        sut.save(updatedQuiz)
        sut.findById(quiz.id).shouldBe(updatedQuiz)
    }

    @Test
    fun `find all`(testContext: VertxTestContext) = testContext.async {
        val quizzes = sut.withTx { conn ->
            (1..10).map {
                Quiz.new(LocalDate.now()).also { quiz -> sut.save(quiz, conn) }
            }
        }

        sut.findAll().shouldContainExactlyInAnyOrder(quizzes)
    }

    @Test
    fun `find by id with stages`(testContext: VertxTestContext) = testContext.async {
        val quiz = Quiz.new(LocalDate.now())
        sut.save(quiz)

        val (stage, _) = saveDummyQuizAndCategory(quiz)
        sut.findByIdWithStages(quiz.id).asClue {
            it.shouldNotBeNull().copy(stages = emptyList()).shouldBe(quiz)
            it.stages.shouldContainExactlyInAnyOrder(stage)
        }
    }

    @Test
    fun `find by id with stages and categories`(testContext: VertxTestContext) = testContext.async {
        val quiz = Quiz.new(LocalDate.now())
        sut.save(quiz)

        val (stage, category) = saveDummyQuizAndCategory(quiz)
        sut.findByIdWithStagesAndCategories(quiz.id).asClue { foundQuiz ->
            foundQuiz.shouldNotBeNull().copy(stages = emptyList()).shouldBe(quiz)
            val foundStage = foundQuiz.stages.shouldHaveSize(1).first()
            foundStage.copy(categories = emptyList()).shouldBe(stage)
            foundStage.categories.shouldContainExactlyInAnyOrder(category)
        }
    }


    private suspend fun saveDummyQuizAndCategory(quiz: Quiz): Pair<QuizStage, QuizCategory> {
        val stage = QuizStage.new(1, "description")
        val category = QuizCategory.new("title", 1, "Context", "Descriptions")
        sut.withTx { conn ->
            stageRepo.save(quiz.id, stage, conn)
            categoryRepo.save(stage.id, category, conn)
        }
        return stage to category
    }
}