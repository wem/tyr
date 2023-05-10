package ch.sourcemotion.tyr.creator.dto.question

import ch.sourcemotion.tyr.creator.dto.element.QuestionElementDto
import kotlinx.serialization.Serializable

@Serializable
data class MultiChoiceQuestionDto<E : QuestionElementDto>(
    val answerElements: List<E>
) : CategoryContextQuestionDto()