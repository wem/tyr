package ch.sourcemotion.tyr.creator.domain.entity

import ch.sourcemotion.tyr.creator.domain.entity.Entity.Companion.ID_COLUMN
import ch.sourcemotion.tyr.creator.domain.entity.question.Question
import ch.sourcemotion.tyr.creator.domain.entity.question.element.Text
import ch.sourcemotion.tyr.creator.domain.entity.question.element.textOf
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class QuizCategory(
    @JsonProperty(ID_COLUMN)
    @JsonAlias(ID_QUERY_REF)
    override val id: UUID,

    @JsonProperty(TITLE_COLUMN)
    @JsonAlias(TITLE_QUERY_REF)
    val title: String,

    @JsonProperty(ORDER_NUMBER_COLUMN)
    @JsonAlias(ORDER_NUMBER_QUERY_REF)
    val orderNumber: Int,

    @JsonProperty(CONTEXT_OR_QUESTION_TEXT_COLUMN)
    @JsonAlias(CONTEXT_OR_QUESTION_TEXT_QUERY_REF)
    val contextOrQuestionText: Text,

    @JsonProperty(QUESTIONS_COLUMN)
    @JsonAlias(QUESTIONS_QUERY_REF)
    val questions: List<Question>
) : Entity {
    companion object {
        const val TABLE = "quiz_category"

        const val TITLE_COLUMN = "title"
        const val CONTEXT_OR_QUESTION_TEXT_COLUMN = "context_or_question_text"
        const val ORDER_NUMBER_COLUMN = "order_number"
        const val QUESTIONS_COLUMN = "questions"

        const val STAGE_FK_COLUMN = "quiz_stage_id"

        const val ID_QUERY_REF = "qc_id"
        const val TITLE_QUERY_REF = "qc_title"
        const val CONTEXT_OR_QUESTION_TEXT_QUERY_REF = "qc_context_or_question_text"
        const val ORDER_NUMBER_QUERY_REF = "qc_order_number"
        const val QUESTIONS_QUERY_REF = "qc_questions"

        const val COLUMN_NAMES_EXP =
            "$ID_COLUMN, $TITLE_COLUMN, $CONTEXT_OR_QUESTION_TEXT_COLUMN, $ORDER_NUMBER_COLUMN, $QUESTIONS_COLUMN, $STAGE_FK_COLUMN"

        const val SELECT_COLUMNS_EXP =
            "qc.$ID_COLUMN AS $ID_QUERY_REF, " +
                    "qc.$TITLE_COLUMN AS $TITLE_QUERY_REF, " +
                    "qc.$CONTEXT_OR_QUESTION_TEXT_COLUMN AS $CONTEXT_OR_QUESTION_TEXT_QUERY_REF, " +
                    "qc.$ORDER_NUMBER_COLUMN AS $ORDER_NUMBER_QUERY_REF, " +
                    "qc.$QUESTIONS_COLUMN AS $QUESTIONS_QUERY_REF"

        const val INSERT_PARAMS_EXP =
            "#{$ID_COLUMN}, #{$TITLE_COLUMN}, #{$CONTEXT_OR_QUESTION_TEXT_COLUMN}, #{$ORDER_NUMBER_COLUMN}, " +
                    "#{$QUESTIONS_COLUMN}, #{$STAGE_FK_COLUMN}"

        const val UPDATE_SET_EXP = "$TITLE_COLUMN = #{$TITLE_COLUMN}, " +
                "$CONTEXT_OR_QUESTION_TEXT_COLUMN = #{$CONTEXT_OR_QUESTION_TEXT_COLUMN}, " +
                "$ORDER_NUMBER_COLUMN = #{$ORDER_NUMBER_COLUMN}, " +
                "$QUESTIONS_COLUMN = #{$QUESTIONS_COLUMN}"

        fun new(
            title: String,
            number: Int,
            contextOrQuestion: String,
            description: String?,
            questions: List<Question>
        ) = QuizCategory(UUID.randomUUID(), title, number, textOf(contextOrQuestion, description), questions)

        fun new(
            title: String,
            number: Int,
            contextOrQuestion: String,
            description: String?,
            vararg questions: Question
        ) = QuizCategory(UUID.randomUUID(), title, number, textOf(contextOrQuestion, description), questions.toList())
    }
}