package ch.sourcemotion.tyr.creator.domain.repository.mapping.fromdb

import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Row
import java.util.*

class QuizCategoryRowMapper : RowWithRelationMapper<QuizCategory> {
    private val categories = HashMap<UUID, JsonObject>()

    override fun mapWithRelation(fullRow: JsonObject): JsonObject? {
        val rawCategoryId = fullRow.getString(QuizCategory.ID_QUERY_REF)
        return if (rawCategoryId != null) {
            val categoryId = UUID.fromString(rawCategoryId)
            categories.getOrElse(categoryId) { fullRow.also { categories[categoryId] = it } }
        } else null
    }

    override fun buildFromRows(): List<QuizCategory> = categories.map { it.value.mapTo(QuizCategory::class.java) }

    override fun map(row: Row) {
        mapWithRelation(row.toJson())
    }
}