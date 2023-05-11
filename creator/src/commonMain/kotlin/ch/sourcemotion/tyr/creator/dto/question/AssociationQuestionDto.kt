package ch.sourcemotion.tyr.creator.dto.question

import ch.sourcemotion.tyr.creator.dto.element.QuestionElementDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("AssociationQuestionDto")
data class AssociationQuestionDto<E : QuestionElementDto>(
    val questionElements: List<E>,
    val answerElements: List<E>
) : CategoryContextQuestionDto()