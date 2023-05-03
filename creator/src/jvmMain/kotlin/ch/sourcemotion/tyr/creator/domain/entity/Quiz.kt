package ch.sourcemotion.tyr.creator.domain.entity

import ch.sourcemotion.tyr.creator.domain.entity.Entity.Companion.ID_COLUMN
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY
import java.time.LocalDate
import java.util.*

data class Quiz(
    @JsonProperty(ID_COLUMN)
    @JsonAlias(ID_QUERY_REF)
    override val id: UUID,

    @JsonProperty(DATE_COLUMN)
    @JsonAlias(DATE_QUERY_REF)
    val date: LocalDate,

    @JsonProperty(STAGES_REL_FIELD_NAME, access = WRITE_ONLY)
    val stages: List<QuizStage> = emptyList()
) : Entity {
    companion object {
        const val TABLE = "quiz"

        const val DATE_COLUMN = "date"

        const val STAGES_REL_FIELD_NAME = "stages"

        const val ID_QUERY_REF = "q_id"
        const val DATE_QUERY_REF = "q_date"

        const val SELECT_COLUMNS_EXP = "q.$ID_COLUMN AS $ID_QUERY_REF, q.$DATE_COLUMN AS $DATE_QUERY_REF"

        const val COLUMN_NAMES_EXP = "$ID_COLUMN, $DATE_COLUMN"

        const val INSERT_PARAMS_EXP = "#{$ID_COLUMN}, #{$DATE_COLUMN}"

        const val UPDATE_SET_EXP = "$DATE_COLUMN = #{$DATE_COLUMN}"

        fun new(date: LocalDate) = Quiz(UUID.randomUUID(), date, emptyList())
    }
}