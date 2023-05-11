package ch.sourcemotion.tyr.creator.domain.service

import ch.sourcemotion.tyr.creator.commandquery.onCommand
import ch.sourcemotion.tyr.creator.commandquery.onQuery
import ch.sourcemotion.tyr.creator.datamapping.toDto
import ch.sourcemotion.tyr.creator.datamapping.toDtos
import ch.sourcemotion.tyr.creator.datamapping.toEntity
import ch.sourcemotion.tyr.creator.domain.repository.QuizRepository
import ch.sourcemotion.tyr.creator.domain.service.QuizService.*
import ch.sourcemotion.tyr.creator.dto.QuizDto
import ch.sourcemotion.tyr.creator.ext.getOrCreateByFactory
import io.vertx.kotlin.coroutines.CoroutineVerticle

class QuizServiceVerticle : CoroutineVerticle(), QuizService {

    private lateinit var repo: QuizRepository

    override suspend fun start() {
        repo = vertx.getOrCreateByFactory()

        val eventBus = vertx.eventBus()
        eventBus.consumer(CreateOrUpdateQuizCmd.address, onCommand(::createOrUpdateQuiz))
        eventBus.consumer(DeleteQuizCmd.address, onCommand(::deleteQuiz))

        eventBus.consumer(GetQuizQuery.address, onQuery(::getQuiz))
        eventBus.consumer(GetQuizzesQuery.address, onQuery(::getQuizzes))
    }

    override suspend fun createOrUpdateQuiz(cmd: CreateOrUpdateQuizCmd) {
        repo.runCatching { save(cmd.quizDto.toEntity()) }.getOrElse { failure ->
            throw QuizServiceException("Failed to create / update quiz", failure)
        }
    }

    override suspend fun deleteQuiz(cmd: DeleteQuizCmd) {
        repo.runCatching { delete(cmd.id) }.getOrElse {  failure ->
            throw QuizServiceException("Failed to delete quiz", failure)
        }
    }

    override suspend fun getQuiz(query: GetQuizQuery): QuizDto? {
        return runCatching {
            (if (query.withStages && query.withCategories) {
                repo.findByIdWithStagesAndCategories(query.id)
            } else if (query.withStages) {
                repo.findByIdWithStages(query.id)
            } else {
                repo.findById(query.id)
            })?.toDto()
        }.getOrElse { failure ->
            throw QuizServiceException("Failed to get quiz", failure)
        }
    }

    override suspend fun getQuizzes(query: GetQuizzesQuery): List<QuizDto> {
        // TODO: Make findAll consistent to query parameters for stages and categories
        return repo.runCatching { findAll().toDtos() }.getOrElse { failure ->
            throw QuizServiceException("Failed to get quizzes", failure)
        }
    }
}

class QuizServiceException(message: String?, cause: Throwable? = null) : ServiceException(message, cause)