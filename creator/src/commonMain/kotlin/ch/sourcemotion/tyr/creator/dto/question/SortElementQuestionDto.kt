package ch.sourcemotion.tyr.creator.dto.question

import ch.sourcemotion.tyr.creator.dto.element.QuestionElementDto
import kotlinx.serialization.Serializable

@Serializable
data class SortElementQuestionDto<E : QuestionElementDto>(
    val questionElements: List<E>,
    val answerElements: List<E>
) : CategoryContextQuestionDto()