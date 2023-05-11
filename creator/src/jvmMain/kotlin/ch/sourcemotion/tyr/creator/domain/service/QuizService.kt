package ch.sourcemotion.tyr.creator.domain.service

import ch.sourcemotion.tyr.creator.commandquery.*
import ch.sourcemotion.tyr.creator.dto.QuizDto
import ch.sourcemotion.tyr.creator.ext.SharedFactory
import ch.sourcemotion.tyr.creator.logging.mdcOf
import io.vertx.core.Vertx
import kotlinx.coroutines.slf4j.MDCContext
import java.util.*

interface QuizService : Service {

    companion object : SharedFactory<QuizService> {
        fun create(vertx: Vertx): QuizService = object : QuizService {
            override fun getVertx() = vertx
        }

        override fun createInstance(vertx: Vertx): QuizService = create(vertx)
    }

    suspend fun createOrUpdateQuiz(cmd: CreateOrUpdateQuizCmd): Unit = getVertx().eventBus().cmdMsg(cmd)

    suspend fun getQuiz(query: GetQuizQuery): QuizDto? = getVertx().eventBus().queryMsg(query)

    suspend fun getQuizzes(query: GetQuizzesQuery): List<QuizDto> = getVertx().eventBus().queryMsg(query)

    suspend fun deleteQuiz(cmd: DeleteQuizCmd) = getVertx().eventBus().cmdMsg(cmd)

    data class CreateOrUpdateQuizCmd(val quizDto: QuizDto) : Cmd {
        companion object : Addressable {
            override val address = "/creator/quiz/cmd/create-or-update"
        }

        override val address = Companion.address

        override fun mdcOf() = mdcOf(quizId = quizDto.id)
    }

    data class GetQuizQuery(val id: UUID, val withStages: Boolean, val withCategories: Boolean) : Query<QuizDto?> {
        companion object : Addressable {
            override val address = "/creator/quiz/query/get-single"
        }

        override val address = Companion.address

        override fun mdcOf() = mdcOf(quizId = id)
    }

    data class GetQuizzesQuery(val withStages: Boolean, val withCategories: Boolean) : Query<List<QuizDto>> {
        companion object : Addressable {
            override val address = "/creator/quiz/query/get-all"
        }

        override val address = Companion.address

        override fun mdcOf() = MDCContext()
    }

    data class DeleteQuizCmd(val id: UUID) : Cmd {
        companion object : Addressable {
            override val address = "/creator/quiz/query/delete"
        }

        override val address = Companion.address

        override fun mdcOf() = mdcOf(quizId = id)
    }
}