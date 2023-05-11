package ch.sourcemotion.tyr.creator.domain.service

import ch.sourcemotion.tyr.creator.commandquery.*
import ch.sourcemotion.tyr.creator.dto.QuizStageDto
import ch.sourcemotion.tyr.creator.ext.SharedFactory
import ch.sourcemotion.tyr.creator.logging.mdcOf
import io.vertx.core.Vertx
import java.util.*

interface QuizStageService : Service {
    companion object : SharedFactory<QuizStageService> {
        fun create(vertx: Vertx): QuizStageService = object : QuizStageService {
            override fun getVertx() = vertx
        }

        override fun createInstance(vertx: Vertx): QuizStageService = create(vertx)
    }


    suspend fun createOrUpdateQuizStage(cmd: CreateOrUpdateQuizStageCmd): Unit = getVertx().eventBus().cmdMsg(cmd)

    suspend fun getQuizStage(query: GetQuizStageQuery): QuizStageDto? = getVertx().eventBus().queryMsg(query)

    suspend fun getQuizStages(query: GetQuizStagesQuery): List<QuizStageDto> = getVertx().eventBus().queryMsg(query)

    suspend fun deleteQuizStage(cmd: DeleteQuizStageCmd) = getVertx().eventBus().cmdMsg(cmd)

    data class CreateOrUpdateQuizStageCmd(val quizId: UUID, val quizStageDto: QuizStageDto) : Cmd {
        companion object : Addressable {
            override val address = "/creator/quiz-stage/cmd/create-or-update"
        }

        override val address = Companion.address

        override fun mdcOf() = mdcOf(quizId = quizId, quizStageId = quizStageDto.id)
    }

    data class GetQuizStageQuery(val id: UUID, val withCategories: Boolean) : Query<QuizStageDto?> {
        companion object : Addressable {
            override val address = "/creator/quiz-stage/query/get-single"
        }

        override val address = Companion.address

        override fun mdcOf() = mdcOf(quizStageId = id)
    }

    data class GetQuizStagesQuery(val quizId: UUID, val withCategories: Boolean) : Query<List<QuizStageDto>> {
        companion object : Addressable {
            override val address = "/creator/quiz-stage/query/get-all"
        }

        override val address = Companion.address

        override fun mdcOf() = mdcOf(quizId = quizId)
    }

    data class DeleteQuizStageCmd(val id: UUID) : Cmd {
        companion object : Addressable {
            override val address = "/creator/quiz-stage/query/delete"
        }

        override val address = Companion.address

        override fun mdcOf() = mdcOf(quizStageId = id)
    }
}