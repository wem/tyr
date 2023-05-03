package ch.sourcemotion.tyr.creator.domain.repository.mapping.fromdb

import ch.sourcemotion.tyr.creator.domain.entity.Quiz
import ch.sourcemotion.tyr.creator.domain.entity.Quiz.Companion.ID_QUERY_REF
import ch.sourcemotion.tyr.creator.domain.entity.Quiz.Companion.STAGES_REL_FIELD_NAME
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Row
import java.util.*

class QuizRowMapper(
    private val stageMapper: QuizStageRowMapper = QuizStageRowMapper()
) : RowWithRelationMapper<Quiz> {

    private val quizzes = HashMap<UUID, JsonObject>()

    override fun map(row: Row) {
        val rowJson = row.toJson()
        val rawQuizId = rowJson.getString(ID_QUERY_REF)
        if (rawQuizId != null) {
            val quizId = UUID.fromString(rawQuizId)
            val quizJson = quizzes.getOrElse(quizId) { rowJson.also { quizzes[quizId] = it } }
            val stageJson = stageMapper.mapWithRelation(rowJson.copy())
            if (stageJson != null) {
                val stageRelations = (quizJson.getJsonArray(STAGES_REL_FIELD_NAME) ?: JsonArray()).apply {
                    add(stageJson)
                }
                quizJson.put(STAGES_REL_FIELD_NAME, stageRelations)
            }
        }
    }

    override fun buildFromRows(): List<Quiz> = quizzes.map { it.value.mapTo(Quiz::class.java) }
}