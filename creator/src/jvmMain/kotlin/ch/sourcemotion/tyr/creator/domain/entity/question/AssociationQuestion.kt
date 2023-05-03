package ch.sourcemotion.tyr.creator.domain.entity.question

import ch.sourcemotion.tyr.creator.domain.entity.question.element.Element
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * Question with text question and (multi) choice answer(s).
 */
data class AssociationQuestion<E : Element>(
    @JsonProperty("question_elements")
    val questionElements: List<E>,
    @JsonProperty("answer_elements")
    val answerElements: List<E>
) : CategoryContextQuestion