package ch.sourcemotion.tyr.creator.domain.entity.question

import ch.sourcemotion.tyr.creator.domain.entity.question.element.Element
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * The question has a list of entries like names, events or something like this. The list is unordered and the answer
 * would be a (re)ordered list of that entries
 */
data class SortElementQuestion<E : Element>(
    @JsonProperty("question_elements")
    val questionElements: List<E>,
    @JsonProperty("answer_elements")
    val answerElements: List<E>
) : CategoryContextQuestion

