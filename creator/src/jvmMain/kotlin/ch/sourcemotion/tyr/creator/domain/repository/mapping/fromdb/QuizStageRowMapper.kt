package ch.sourcemotion.tyr.creator.domain.repository.mapping.fromdb

import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage.Companion.CATEGORIES_REL_FIELD_NAME
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Row
import java.util.*

class QuizStageRowMapper(
    private val categoryMapper: QuizCategoryRowMapper = QuizCategoryRowMapper()
) : RowWithRelationMapper<QuizStage> {

    private val stages = HashMap<UUID, JsonObject>()

    override fun mapWithRelation(fullRow: JsonObject): JsonObject? {
        val rawStageId = fullRow.getString(QuizStage.ID_QUERY_REF)
        return if (rawStageId != null) {
            val stageId = UUID.fromString(rawStageId)
            val stageJson: JsonObject = stages.getOrElse(stageId) { fullRow.also { stages[stageId] = it } }

            val categoryJson = categoryMapper.mapWithRelation(fullRow.copy())
            if (categoryJson != null) {
                val categories = stageJson.getJsonArray(CATEGORIES_REL_FIELD_NAME, JsonArray()).apply {
                    add(categoryJson)
                }
                stageJson.put(CATEGORIES_REL_FIELD_NAME, categories)
            }
            stageJson
        } else null
    }

    override fun map(row: Row) {
        mapWithRelation(row.toJson())
    }


    override fun buildFromRows(): List<QuizStage> = stages.map { it.value.mapTo(QuizStage::class.java) }
}