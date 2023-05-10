package ch.sourcemotion.tyr.creator.dto.question

import ch.sourcemotion.tyr.creator.dto.element.TextDto
import kotlinx.serialization.Serializable

@Serializable
sealed class QuestionDto

@Serializable
sealed class CategoryContextQuestionDto : QuestionDto()

@Serializable
sealed class IndividualTextQuestionDto : QuestionDto() {
    abstract val question: TextDto
}