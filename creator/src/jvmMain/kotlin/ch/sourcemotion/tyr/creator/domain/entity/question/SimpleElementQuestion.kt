package ch.sourcemotion.tyr.creator.domain.entity.question

import ch.sourcemotion.tyr.creator.domain.entity.question.element.Element
import ch.sourcemotion.tyr.creator.domain.entity.question.element.Text
import java.util.*

/**
 * Question with a text and a simple, single answer in text form.
 */
data class SimpleElementQuestion<E : Element>(
    override val question: Text,
    val answer: E
) : IndividualTextQuestion