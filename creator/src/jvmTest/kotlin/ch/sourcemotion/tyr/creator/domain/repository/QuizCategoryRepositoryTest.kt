package ch.sourcemotion.tyr.creator.domain.repository

import ch.sourcemotion.tyr.creator.domain.MimeType
import ch.sourcemotion.tyr.creator.domain.entity.Quiz
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage
import ch.sourcemotion.tyr.creator.domain.entity.question.AssociationQuestion
import ch.sourcemotion.tyr.creator.domain.entity.question.MultiChoiceQuestion
import ch.sourcemotion.tyr.creator.domain.entity.question.SimpleElementQuestion
import ch.sourcemotion.tyr.creator.domain.entity.question.SortElementQuestion
import ch.sourcemotion.tyr.creator.domain.entity.question.element.Image
import ch.sourcemotion.tyr.creator.domain.entity.question.element.Sound
import ch.sourcemotion.tyr.creator.domain.entity.question.element.textOf
import ch.sourcemotion.tyr.creator.ext.getOrCreateByFactory
import ch.sourcemotion.tyr.creator.testing.AbstractVertxDatabaseTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

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
        val question = SimpleElementQuestion(textOf("Question text"), textOf("Answer"))
        val category = QuizCategory.new("title", 1, "Category question", "description", question)

        val (_, quizStage) = saveDummyQuizAndStage()

        sut.save(quizStage.id, category)
        sut.findById(category.id).shouldBe(category)
        sut.delete(category.id)
        sut.findById(category.id).shouldBeNull()
    }

    @Test
    fun `save and update`(testContext: VertxTestContext) = testContext.async {
        val question = SimpleElementQuestion(textOf("Question text"), textOf("Answer"))
        val category = QuizCategory.new("title", 1, "Category question", "description", question)
        val (_, quizStage) = saveDummyQuizAndStage()

        sut.save(quizStage.id, category)
        sut.findById(category.id).shouldBe(category)

        val updatedCategory = category.copy(
            title = "New title",
            orderNumber = 2,
            contextOrQuestionText = textOf("New category question"),
            questions = category.questions + SimpleElementQuestion(
                textOf("Another question text"),
                textOf("Another answer")
            )
        )
        sut.save(quizStage.id, updatedCategory)
        sut.findById(category.id).shouldBe(updatedCategory)
    }

    @Test
    fun `save with any question variant`(testContext: VertxTestContext) = testContext.async {
        val (_, quizStage) = saveDummyQuizAndStage()

        val simpleTextQuestion = SimpleElementQuestion(textOf("Question"), textOf("Answer"))
        val simpleImageQuestion = SimpleElementQuestion(textOf("Question text"), newJpeg())
        val simpleSoundQuestion = SimpleElementQuestion(textOf("Question text"), newMp3())

        val textSortQuestion = SortElementQuestion(listOf(textOf("Question text")), listOf(textOf("Question text")))
        val imageSortQuestion = SortElementQuestion(listOf(newJpeg()), listOf(newJpeg()))
        val soundSortQuestion = SortElementQuestion(listOf(newMp3()), listOf(newMp3()))

        val textAssociationQuestion = AssociationQuestion(listOf(textOf("Question text")), listOf(textOf("Question text")))
        val imageAssociationQuestion = AssociationQuestion(listOf(newJpeg()), listOf(newJpeg()))
        val soundAssociationQuestion = AssociationQuestion(listOf(newMp3()), listOf(newMp3()))

        val textMultiChoiceQuestion = MultiChoiceQuestion(listOf(textOf("Question text")))
        val imageMultiChoiceQuestion = MultiChoiceQuestion(listOf(newJpeg()))
        val soundMultiChoiceQuestion = MultiChoiceQuestion(listOf(newMp3()))

        val category = QuizCategory.new(
            "title", 1, "Category question", "description",
            simpleTextQuestion,
            simpleImageQuestion,
            simpleSoundQuestion,
            textSortQuestion,
            imageSortQuestion,
            soundSortQuestion,
            textAssociationQuestion,
            imageAssociationQuestion,
            soundAssociationQuestion,
            textMultiChoiceQuestion,
            imageMultiChoiceQuestion,
            soundMultiChoiceQuestion
        )

        sut.save(quizStage.id, category)
        sut.findById(category.id).shouldBe(category)
    }

    @Test
    fun `find all of stage`(testContext: VertxTestContext) = testContext.async {
        val question = SimpleElementQuestion(textOf("Question text"), textOf("Answer"))
        val (_, quizStage) = saveDummyQuizAndStage()
        val (_, anotherStage) = saveDummyQuizAndStage()

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
        sut.findAllOfStage(anotherStage.id).shouldBeEmpty()
    }

    private fun newJpeg() = Image(UUID.randomUUID(), MimeType.JPEG, "Image description")

    private fun newMp3() = Sound(UUID.randomUUID(), MimeType.MP3, "Sound description")

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