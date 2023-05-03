package ch.sourcemotion.tyr.creator.domain.repository.mapping.todb

import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.*
import java.util.function.Function

class QuizCategoryTupleMapper(private val stageId: UUID) : Function<QuizCategory, Map<String, Any>> {
    override fun apply(quizCategory: QuizCategory): Map<String, Any> {
        return JsonObject.mapFrom(quizCategory)
            .put(QuizCategory.STAGE_FK_COLUMN, stageId)
            .put(QuizCategory.CONTEXT_OR_QUESTION_TEXT_COLUMN, JsonObject.mapFrom(quizCategory.contextOrQuestionText))
            .put(QuizCategory.QUESTIONS_COLUMN, JsonArray(quizCategory.questions.map { JsonObject.mapFrom(it) }))
            .map
    }
}