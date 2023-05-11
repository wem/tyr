package ch.sourcemotion.tyr.creator.domain.entity.question

import ch.sourcemotion.tyr.creator.domain.entity.question.element.Text
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_question_type")
@JsonSubTypes(
    JsonSubTypes.Type(AssociationQuestion::class, name = "association"),
    JsonSubTypes.Type(MultiChoiceQuestion::class, name = "multichoice"),
    JsonSubTypes.Type(SimpleElementQuestion::class, name = "simple"),
    JsonSubTypes.Type(SortElementQuestion::class, name = "sort"),
)
sealed interface Question

/**
 * Question that uses / inherits the question text from category
 */
sealed interface CategoryContextQuestion: Question

/**
 * Question has its individual question text like category "Answer some question about history".
 */
sealed interface IndividualTextQuestion : Question {
    val question: Text
}