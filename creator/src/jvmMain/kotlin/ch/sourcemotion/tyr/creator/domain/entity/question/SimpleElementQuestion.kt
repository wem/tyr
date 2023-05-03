package ch.sourcemotion.tyr.creator.domain.entity.question

import ch.sourcemotion.tyr.creator.domain.entity.question.element.QuestionElement
import ch.sourcemotion.tyr.creator.domain.entity.question.element.Text

/**
 * Question with a text and a simple, single answer in text form.
 */
data class SimpleElementQuestion<E : QuestionElement>(
    override val question: Text,
    val answer: E
) : IndividualTextQuestion