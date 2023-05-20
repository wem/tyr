package ch.sourcemotion.tyr.creator.domain.repository.mapping.fromdb

import ch.sourcemotion.tyr.creator.domain.entity.QuizStage
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage.Companion.CATEGORIES_REL_FIELD_NAME
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Row
import java.util.*

class QuizStageRowMapper(
    private val categoryMapper: QuizCategoryRowMapper = QuizCategoryRowMapper()
) : RowWithRelationMapper<QuizStage, StageJson> {

    private val stages = HashMap<UUID, JsonObject>()
    private val assignedCategories = HashMap<UUID, MutableList<UUID>>()

    override fun mapWithRelation(fullRow: JsonObject): StageJson? {
        val rawStageId = fullRow.getString(QuizStage.ID_QUERY_REF)
        return if (rawStageId != null) {
            val stageId = UUID.fromString(rawStageId)
            val stageJson: JsonObject = stages.getOrElse(stageId) { fullRow.also { stages[stageId] = it } }

            val categoryJson = categoryMapper.mapWithRelation(fullRow.copy())
            if (categoryJson != null) {
                val assignedCategoriesActualStage = assignedCategories.getOrPut(stageId) { ArrayList() }
                if (!assignedCategoriesActualStage.contains(stageId)) {
                    val categories = stageJson.getJsonArray(CATEGORIES_REL_FIELD_NAME, JsonArray()).apply {
                        add(categoryJson)
                    }
                    stageJson.put(CATEGORIES_REL_FIELD_NAME, categories)
                    assignedCategoriesActualStage.add(categoryJson.id)
                }
            }
            StageJson(stageJson)
        } else null
    }

    override fun map(row: Row) {
        mapWithRelation(row.toJson())
    }


    override fun buildFromRows(): List<QuizStage> = stages.map { it.value.mapTo(QuizStage::class.java) }
}

class StageJson(raw: JsonObject) : JsonObject(raw.map) {
    val id: UUID = UUID.fromString(raw.getString(QuizStage.ID_QUERY_REF))
}