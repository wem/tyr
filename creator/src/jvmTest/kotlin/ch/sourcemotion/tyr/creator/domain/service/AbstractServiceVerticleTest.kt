package ch.sourcemotion.tyr.creator.domain.service

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
import ch.sourcemotion.tyr.creator.ext.newUUID
import ch.sourcemotion.tyr.creator.testing.AbstractVertxTest
import java.time.LocalDate

abstract class AbstractServiceVerticleTest : AbstractVertxTest() {

    companion object {
        val quizJune = Quiz(newUUID(), LocalDate.parse("2023-06-01"), (1..3).map { stageNumber ->
            QuizStage(newUUID(), stageNumber, "stage_june_$stageNumber", (1..8).map { categoryNumber ->
                QuizCategory(
                    newUUID(),
                    "category_june_${stageNumber}_${categoryNumber}",
                    categoryNumber,
                    textOf(
                        "category_text_june_${stageNumber}_${categoryNumber}",
                        "category_description_june_${stageNumber}_${categoryNumber}"
                    ), listOf(
                        SimpleElementQuestion(
                            textOf("question_june_1", "question_description_june_1"),
                            textOf("answer_june_1", "answer_description_june_1")
                        ),
                        MultiChoiceQuestion(listOf(Image(newUUID(), MimeType.JPEG, "description"))),
                        SortElementQuestion(
                            listOf(Sound(newUUID(), MimeType.MP3, "description")),
                            listOf(Sound(newUUID(), MimeType.MP3, "description")),
                        ),
                        AssociationQuestion(
                            listOf(textOf("question_june_4", "question_description_june_4")),
                            listOf(textOf("answer_june_4", "answer_description_june_4"))
                        )
                    )
                )
            })
        })

        val quizJuly = Quiz(newUUID(), LocalDate.parse("2023-07-06"), (1..4).map { stageNumber ->
            QuizStage(newUUID(), stageNumber, "stage_july_$stageNumber", (1..12).map { categoryNumber ->
                QuizCategory(
                    newUUID(),
                    "category_july_${stageNumber}_${categoryNumber}",
                    categoryNumber,
                    textOf(
                        "category_text_july_${stageNumber}_${categoryNumber}",
                        "category_description_july_${stageNumber}_${categoryNumber}"
                    ), listOf(
                        SimpleElementQuestion(
                            textOf("question_july_1", "question_description_july_1"),
                            textOf("answer_july_1", "answer_description_july_1")
                        ),
                        MultiChoiceQuestion(listOf(Image(newUUID(), MimeType.JPEG, "description"))),
                        SortElementQuestion(
                            listOf(Sound(newUUID(), MimeType.MP3, "description")),
                            listOf(Sound(newUUID(), MimeType.MP3, "description")),
                        ),
                        AssociationQuestion(
                            listOf(textOf("question_july_4", "question_description_july_4")),
                            listOf(textOf("answer_july_4", "answer_description_july_4"))
                        )
                    )
                )
            })
        })
    }
}