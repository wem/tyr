package ch.sourcemotion.tyr.creator.domain.repository.mapping.todb

import ch.sourcemotion.tyr.creator.domain.entity.Quiz
import io.vertx.core.json.JsonObject
import java.util.function.Function

object QuizTupleMapper : Function<Quiz, Map<String, Any>> {
    override fun apply(quiz: Quiz): Map<String, Any> {
        return JsonObject.mapFrom(quiz)
            .put(Quiz.DATE_COLUMN, quiz.date)
            .map
    }
}