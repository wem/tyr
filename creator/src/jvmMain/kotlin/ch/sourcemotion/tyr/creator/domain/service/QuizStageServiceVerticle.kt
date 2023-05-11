package ch.sourcemotion.tyr.creator.domain.service

import ch.sourcemotion.tyr.creator.commandquery.onCommand
import ch.sourcemotion.tyr.creator.commandquery.onQuery
import ch.sourcemotion.tyr.creator.datamapping.toDto
import ch.sourcemotion.tyr.creator.datamapping.toDtos
import ch.sourcemotion.tyr.creator.datamapping.toEntity
import ch.sourcemotion.tyr.creator.domain.repository.QuizStageRepository
import ch.sourcemotion.tyr.creator.domain.service.QuizStageService.*
import ch.sourcemotion.tyr.creator.dto.QuizStageDto
import ch.sourcemotion.tyr.creator.ext.getOrCreateByFactory
import io.vertx.kotlin.coroutines.CoroutineVerticle

class QuizStageServiceVerticle : CoroutineVerticle(), QuizStageService {

    private lateinit var repo: QuizStageRepository

    override suspend fun start() {
        repo = vertx.getOrCreateByFactory()

        val eventBus = vertx.eventBus()
        eventBus.consumer(CreateOrUpdateQuizStageCmd.address, onCommand(::createOrUpdateQuizStage))
        eventBus.consumer(DeleteQuizStageCmd.address, onCommand(::deleteQuizStage))

        eventBus.consumer(GetQuizStageQuery.address, onQuery(::getQuizStage))
        eventBus.consumer(GetQuizStagesQuery.address, onQuery(::getQuizStages))
    }

    override suspend fun createOrUpdateQuizStage(cmd: CreateOrUpdateQuizStageCmd) {
        repo.runCatching { save(cmd.quizId, cmd.quizStageDto.toEntity()) }.getOrElse { failure ->
            throw QuizStageServiceException("Failed to create / update quiz stage", failure)
        }
    }

    override suspend fun deleteQuizStage(cmd: DeleteQuizStageCmd) {
        repo.runCatching { delete(cmd.id) }.getOrElse { failure ->
            throw QuizStageServiceException("Failed to delete quiz stage", failure)
        }
    }

    override suspend fun getQuizStage(query: GetQuizStageQuery): QuizStageDto? {
        return repo.runCatching { findById(query.id)?.toDto() }.getOrElse { failure ->
            throw QuizStageServiceException("Failed to get quiz stage", failure)
        }
    }

    override suspend fun getQuizStages(query: GetQuizStagesQuery): List<QuizStageDto> {
        return repo.runCatching { findAllOfQuiz(query.quizId).toDtos() }.getOrElse { failure ->
            throw QuizStageServiceException("Failed to get quiz stage", failure)
        }
    }
}

class QuizStageServiceException(message: String?, cause: Throwable? = null) : ServiceException(message, cause)