package ch.sourcemotion.tyr.creator.domain.entity

import ch.sourcemotion.tyr.creator.domain.entity.Entity.Companion.ID_COLUMN
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY
import java.util.*

data class QuizStage(
    @JsonProperty(ID_COLUMN)
    @JsonAlias(ID_QUERY_REF)
    override val id: UUID,

    @JsonProperty(NUMBER_COLUMN)
    @JsonAlias(NUMBER_QUERY_REF)
    val number: Int,

    @JsonProperty(DESCRIPTION_COLUMN)
    @JsonAlias(DESCRIPTION_QUERY_REF)
    val description: String?,

    @JsonProperty(CATEGORIES_REL_FIELD_NAME, access = WRITE_ONLY)
    val categories: List<QuizCategory> = emptyList()
) : Entity {

    companion object {
        const val TABLE = "quiz_stage"

        const val NUMBER_COLUMN = "number"
        const val DESCRIPTION_COLUMN = "description"

        const val QUIZ_FK_COLUMN = "quiz_id"

        const val CATEGORIES_REL_FIELD_NAME = "categories"

        const val ID_QUERY_REF = "qs_id"
        const val NUMBER_QUERY_REF = "qs_number"
        const val DESCRIPTION_QUERY_REF = "qs_description"

        const val SELECT_COLUMNS_EXP = "qs.$ID_COLUMN AS $ID_QUERY_REF, qs.$NUMBER_COLUMN AS $NUMBER_QUERY_REF, " +
                "qs.$DESCRIPTION_COLUMN AS $DESCRIPTION_QUERY_REF"

        const val COLUMN_NAMES_EXP = "$ID_COLUMN, $NUMBER_COLUMN, $DESCRIPTION_COLUMN, $QUIZ_FK_COLUMN"

        const val INSERT_PARAMS_EXP = "#{$ID_COLUMN}, #{$NUMBER_COLUMN}, #{$DESCRIPTION_COLUMN}, #{$QUIZ_FK_COLUMN}"

        const val UPDATE_SET_EXP = "$NUMBER_COLUMN = #{$NUMBER_COLUMN}, $DESCRIPTION_COLUMN = #{$DESCRIPTION_COLUMN}"

        fun new(number: Int, description: String? = null) =
            QuizStage(UUID.randomUUID(), number, description, emptyList())
    }
}