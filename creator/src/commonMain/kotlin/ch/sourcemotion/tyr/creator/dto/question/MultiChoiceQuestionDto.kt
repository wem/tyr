package ch.sourcemotion.tyr.creator.dto.question

import ch.sourcemotion.tyr.creator.dto.element.QuestionElementDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MultiChoiceQuestionDto")
data class MultiChoiceQuestionDto<E : QuestionElementDto>(
    val answerElements: List<E>
) : CategoryContextQuestionDto()