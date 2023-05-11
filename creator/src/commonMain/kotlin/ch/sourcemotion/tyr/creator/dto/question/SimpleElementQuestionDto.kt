package ch.sourcemotion.tyr.creator.dto.question

import ch.sourcemotion.tyr.creator.dto.element.QuestionElementDto
import ch.sourcemotion.tyr.creator.dto.element.TextDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SimpleElementQuestionDto")
data class SimpleElementQuestionDto<E : QuestionElementDto>(
    override val question: TextDto,
    val answer: E
) : IndividualTextQuestionDto()