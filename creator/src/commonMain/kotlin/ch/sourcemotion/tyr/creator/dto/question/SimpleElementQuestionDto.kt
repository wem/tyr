package ch.sourcemotion.tyr.creator.dto.question

import ch.sourcemotion.tyr.creator.dto.element.QuestionElementDto
import ch.sourcemotion.tyr.creator.dto.element.TextDto
import kotlinx.serialization.Serializable

@Serializable
data class SimpleElementQuestionDto<E : QuestionElementDto>(
    override val question: TextDto,
    val answer: E
) : IndividualTextQuestionDto()