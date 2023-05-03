package ch.sourcemotion.tyr.creator.domain.entity

import ch.sourcemotion.tyr.creator.domain.MimeType
import ch.sourcemotion.tyr.creator.domain.entity.Entity.Companion.ID_COLUMN
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class FileInfoEntity(
    @JsonProperty(ID_COLUMN)
    @JsonAlias(ID_QUERY_REF)
    override val id: UUID,

    @JsonProperty(MIME_TYPE_COLUMN)
    @JsonAlias(MIME_TYPE_QUERY_REF)
    val mimeType: MimeType,

    @JsonProperty(DESCRIPTION_COLUMN)
    @JsonAlias(DESCRIPTION_QUERY_REF)
    val description: String?,
) : Entity {
    companion object {
        const val TABLE = "file_info"

        const val MIME_TYPE_COLUMN = "mime_type"
        const val DESCRIPTION_COLUMN = "description"

        const val ID_QUERY_REF = "f_id"
        const val MIME_TYPE_QUERY_REF = "f_mime_type"
        const val DESCRIPTION_QUERY_REF = "f_description"

        const val SELECT_COLUMNS_EXP = "f.$ID_COLUMN AS $ID_QUERY_REF, " +
                "f.$MIME_TYPE_COLUMN AS $MIME_TYPE_QUERY_REF, " +
                "f.$DESCRIPTION_COLUMN AS $DESCRIPTION_QUERY_REF"

        const val COLUMN_NAMES_EXP = "$ID_COLUMN, $MIME_TYPE_COLUMN, $DESCRIPTION_COLUMN"

        const val INSERT_PARAMS_EXP = "#{$ID_COLUMN}, #{$MIME_TYPE_COLUMN}, #{$DESCRIPTION_COLUMN}"

        const val UPDATE_SET_EXP = "$MIME_TYPE_COLUMN = #{$MIME_TYPE_COLUMN}, " +
                "$DESCRIPTION_COLUMN = #{$DESCRIPTION_COLUMN}"
    }
}