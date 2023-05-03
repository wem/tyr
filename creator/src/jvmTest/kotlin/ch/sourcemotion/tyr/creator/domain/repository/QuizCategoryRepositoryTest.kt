package ch.sourcemotion.tyr.creator.domain.repository

import ch.sourcemotion.tyr.creator.domain.entity.Quiz
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage
import ch.sourcemotion.tyr.creator.domain.entity.question.SimpleElementQuestion
import ch.sourcemotion.tyr.creator.domain.entity.question.element.Text
import ch.sourcemotion.tyr.creator.ext.getOrCreateByFactory
import ch.sourcemotion.tyr.creator.testing.AbstractVertxDatabaseTest
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class QuizCategoryRepositoryTest : AbstractVertxDatabaseTest() {

    private lateinit var quizRepo: QuizRepository
    private lateinit var quizStageRepo: QuizStageRepository
    private lateinit var sut: QuizCategoryRepository

    @BeforeEach
    fun setUpSut() {
        quizRepo = QuizRepository(vertx.getOrCreateByFactory())
        quizStageRepo = QuizStageRepository(vertx.getOrCreateByFactory())
        sut = QuizCategoryRepository(vertx.getOrCreateByFactory())
    }

    @Test
    fun `save find by id and delete`(testContext: VertxTestContext) = testContext.async {
        val question = SimpleElementQuestion(Text.textOf("Question text"), Text.textOf("Answer"))
        val category = QuizCategory.new("title", 1, "Category question", "description", question)

        val (_, quizStage) = saveDummyQuizAndStage()

        sut.save(quizStage.id, category)
        sut.findById(category.id).shouldBe(category)
        sut.delete(category.id)
        sut.findById(category.id).shouldBeNull()
    }

    @Test
    fun `save and update`(testContext: VertxTestContext) = testContext.async {
        val question = SimpleElementQuestion(Text.textOf("Question text"), Text.textOf("Answer"))
        val category = QuizCategory.new("title", 1, "Category question", "description", question)
        val (_, quizStage) = saveDummyQuizAndStage()

        sut.save(quizStage.id, category)
        sut.findById(category.id).shouldBe(category)

        val updatedCategory = category.copy(
            title = "New title",
            number = 2,
            contextOrQuestionText = Text.textOf("New category question"),
            questions = category.questions + SimpleElementQuestion(
                Text.textOf("Another question text"),
                Text.textOf("Another answer")
            )
        )
        sut.save(quizStage.id, updatedCategory)
        sut.findById(category.id).shouldBe(updatedCategory)
    }

    @Test
    fun `find all of stage`(testContext: VertxTestContext) = testContext.async {
        val question = SimpleElementQuestion(Text.textOf("Question text"), Text.textOf("Answer"))
        val (_, quizStage) = saveDummyQuizAndStage()

        val categories = sut.withTx { conn ->
            (1..10).map { number ->
                QuizCategory.new(
                    "title_$number",
                    number,
                    "Category question $number",
                    "description $number",
                    question
                ).also { category -> sut.save(quizStage.id, category, conn) }
            }
        }

        sut.findAllOfStage(quizStage.id).shouldContainExactlyInAnyOrder(categories)
    }


    private suspend fun saveDummyQuizAndStage(): Pair<Quiz, QuizStage> {
        val quiz = Quiz.new(LocalDate.now())
        val quizStage = QuizStage.new(1, "description")
        sut.withTx { conn ->
            quizRepo.save(quiz, conn)
            quizStageRepo.save(quiz.id, quizStage, conn)
        }
        return quiz to quizStage
    }
}