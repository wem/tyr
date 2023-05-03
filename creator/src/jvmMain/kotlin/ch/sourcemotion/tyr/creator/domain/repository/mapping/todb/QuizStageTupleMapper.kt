package ch.sourcemotion.tyr.creator.domain.repository.mapping.todb

import ch.sourcemotion.tyr.creator.domain.entity.QuizStage
import io.vertx.core.json.JsonObject
import java.util.*
import java.util.function.Function

class QuizStageTupleMapper(private val quizId: UUID) : Function<QuizStage, Map<String, Any>> {
    override fun apply(quizStage: QuizStage): Map<String, Any> {
        return JsonObject.mapFrom(quizStage)
            .put(QuizStage.QUIZ_FK_COLUMN, quizId)
            .map
    }
}