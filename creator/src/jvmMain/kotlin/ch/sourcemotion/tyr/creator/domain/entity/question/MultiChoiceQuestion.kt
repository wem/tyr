package ch.sourcemotion.tyr.creator.domain.entity.question

import ch.sourcemotion.tyr.creator.domain.entity.question.element.Element
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * Question with text question and (multi) choice answer(s).
 */
data class MultiChoiceQuestion<E : Element>(
    @JsonProperty("answer_elements")
    val answerElements: List<E>
) : CategoryContextQuestion