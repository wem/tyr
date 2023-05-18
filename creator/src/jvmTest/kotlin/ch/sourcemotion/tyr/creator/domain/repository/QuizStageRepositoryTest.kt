package ch.sourcemotion.tyr.creator.domain.repository

import ch.sourcemotion.tyr.creator.domain.entity.Quiz
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage
import ch.sourcemotion.tyr.creator.ext.getOrCreateByFactory
import ch.sourcemotion.tyr.creator.testing.AbstractVertxDatabaseTest
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class QuizStageRepositoryTest : AbstractVertxDatabaseTest() {

    private lateinit var quizRepo: QuizRepository
    private lateinit var categoryRepo: QuizCategoryRepository

    private lateinit var sut: QuizStageRepository

    @BeforeEach
    fun setUpSut() {
        quizRepo = QuizRepository(vertx.getOrCreateByFactory())
        categoryRepo = QuizCategoryRepository(vertx.getOrCreateByFactory())
        sut = QuizStageRepository(vertx.getOrCreateByFactory())
    }

    @Test
    fun `save find by id and delete`(testContext: VertxTestContext) = testContext.async {
        val quiz = saveDummyQuiz()
        val stage = QuizStage.new(1, "description")

        sut.save(quiz.id, stage)
        sut.findById(stage.id).shouldBe(stage)
        sut.delete(stage.id)
        sut.findById(stage.id).shouldBeNull()
    }

    @Test
    fun `save and update`(testContext: VertxTestContext) = testContext.async {
        val quiz = saveDummyQuiz()
        val stage = QuizStage.new(1, "description")

        sut.save(quiz.id, stage)
        sut.findById(stage.id).shouldBe(stage)

        val updatedStage = stage.copy(orderNumber = 2, description = "New description")
        sut.save(quiz.id, updatedStage)
        sut.findById(stage.id).shouldBe(updatedStage)
    }

    @Test
    fun `find all of quiz`(testContext: VertxTestContext) = testContext.async {
        val quiz = saveDummyQuiz()
        val anotherQuiz = saveDummyQuiz()

        val stages = sut.withTx {
            (1..10).map { number ->
                QuizStage.new(number, "description_$number").also { stage ->
                    sut.save(quiz.id, stage)
                }
            }
        }

        sut.findAllOfQuiz(quiz.id).shouldContainExactlyInAnyOrder(stages)

        sut.findAllOfQuiz(anotherQuiz.id).shouldBeEmpty()
    }

    @Test
    fun `find by id with categories`(testContext: VertxTestContext) = testContext.async {
        val quiz = saveDummyQuiz()
        val stage = QuizStage.new(1, "description")

        val categories = sut.withTx { conn ->
            sut.save(quiz.id, stage, conn)
            (1..10).map { number ->
                QuizCategory.new("title_$number", number, "context_$number", "description_$number")
                    .also { category -> categoryRepo.save(stage.id, category, conn) }
            }
        }

        sut.findByIdWithCategories(stage.id).shouldNotBeNull().asClue {
            it.categories.shouldContainExactlyInAnyOrder(categories)
            it.copy(categories = emptyList()).shouldBe(stage)
        }
    }


    private suspend fun saveDummyQuiz(): Quiz {
        val quiz = Quiz.new(LocalDate.now())
        quizRepo.save(quiz)
        return quiz
    }
}