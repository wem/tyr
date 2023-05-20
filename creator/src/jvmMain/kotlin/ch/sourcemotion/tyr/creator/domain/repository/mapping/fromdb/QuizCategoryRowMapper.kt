package ch.sourcemotion.tyr.creator.domain.repository.mapping.fromdb

import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Row
import java.util.*

class QuizCategoryRowMapper : RowWithRelationMapper<QuizCategory, CategoryJson> {
    private val categories = HashMap<UUID, CategoryJson>()

    override fun mapWithRelation(fullRow: JsonObject): CategoryJson? {
        val rawCategoryId = fullRow.getString(QuizCategory.ID_QUERY_REF)
        return if (rawCategoryId != null) {
            val categoryId = UUID.fromString(rawCategoryId)
            categories.getOrElse(categoryId) { CategoryJson(fullRow).also { categories[categoryId] = it } }
        } else null
    }

    override fun buildFromRows(): List<QuizCategory> = categories.map { it.value.mapTo(QuizCategory::class.java) }

    override fun map(row: Row) {
        mapWithRelation(row.toJson())
    }
}

class CategoryJson(raw: JsonObject) : JsonObject(raw.map) {
    val id: UUID = UUID.fromString(raw.getString(QuizCategory.ID_QUERY_REF))
}