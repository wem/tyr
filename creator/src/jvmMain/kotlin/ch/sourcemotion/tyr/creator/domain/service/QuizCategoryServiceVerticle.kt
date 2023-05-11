package ch.sourcemotion.tyr.creator.domain.service

import ch.sourcemotion.tyr.creator.commandquery.onCommand
import ch.sourcemotion.tyr.creator.commandquery.onQuery
import ch.sourcemotion.tyr.creator.datamapping.toDto
import ch.sourcemotion.tyr.creator.datamapping.toDtos
import ch.sourcemotion.tyr.creator.datamapping.toEntity
import ch.sourcemotion.tyr.creator.domain.repository.QuizCategoryRepository
import ch.sourcemotion.tyr.creator.domain.service.QuizCategoryService.*
import ch.sourcemotion.tyr.creator.dto.QuizCategoryDto
import ch.sourcemotion.tyr.creator.ext.getOrCreateByFactory
import io.vertx.kotlin.coroutines.CoroutineVerticle

class QuizCategoryServiceVerticle : CoroutineVerticle(), QuizCategoryService {

    private lateinit var repo: QuizCategoryRepository

    override suspend fun start() {
        repo = vertx.getOrCreateByFactory()
        val eventBus = vertx.eventBus()
        eventBus.consumer(CreateOrUpdateQuizCategoryCmd.address, onCommand(::createOrUpdateQuizCategory))
        eventBus.consumer(DeleteQuizCategoryCmd.address, onCommand(::deleteQuizCategory))

        eventBus.consumer(GetQuizCategoryQuery.address, onQuery(::getQuizCategory))
        eventBus.consumer(GetQuizCategoriesQuery.address, onQuery(::getQuizCategories))
    }

    override suspend fun createOrUpdateQuizCategory(cmd: CreateOrUpdateQuizCategoryCmd) {
        repo.runCatching { save(cmd.quizStageId, cmd.quizCategoryDto.toEntity()) }
            .getOrElse { failure ->
                throw QuizCategoryServiceException("Failed to create / update quiz category", failure)
            }
    }

    override suspend fun deleteQuizCategory(cmd: DeleteQuizCategoryCmd) {
        repo.runCatching { delete(cmd.id) }.getOrElse { failure ->
            throw QuizCategoryServiceException("Failed to delete quiz category", failure)
        }
    }

    override suspend fun getQuizCategory(query: GetQuizCategoryQuery): QuizCategoryDto? {
        return repo.runCatching { findById(query.id)?.toDto() }.getOrElse { failure ->
            throw QuizCategoryServiceException("Failed to get quiz category", failure)
        }
    }

    override suspend fun getQuizCategories(query: GetQuizCategoriesQuery): List<QuizCategoryDto> {
        return repo.runCatching { findAllOfStage(query.quizStageId).toDtos() }.getOrElse { failure ->
            throw QuizCategoryServiceException("Failed to get quiz categories", failure)
        }
    }
}

class QuizCategoryServiceException(message: String?, cause: Throwable? = null) : ServiceException(message, cause)