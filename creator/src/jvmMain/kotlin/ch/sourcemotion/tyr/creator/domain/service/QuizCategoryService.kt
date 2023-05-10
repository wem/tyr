package ch.sourcemotion.tyr.creator.domain.service

import ch.sourcemotion.tyr.creator.commandquery.*
import ch.sourcemotion.tyr.creator.dto.QuizCategoryDto
import ch.sourcemotion.tyr.creator.ext.SharedFactory
import io.vertx.core.Vertx
import java.util.*

interface QuizCategoryService : Service {
    companion object : SharedFactory<QuizCategoryService> {
        fun create(vertx: Vertx): QuizCategoryService = object : QuizCategoryService {
            override fun getVertx() = vertx
        }

        override fun createInstance(vertx: Vertx): QuizCategoryService = create(vertx)
    }


    suspend fun createOrUpdateQuizCategory(cmd: CreateOrUpdateQuizCategoryCmd): Unit = getVertx().eventBus().cmdMsg(cmd)

    suspend fun getQuizCategory(query: GetQuizCategoryQuery): QuizCategoryDto? = getVertx().eventBus().queryMsg(query)

    suspend fun getQuizCategories(query: GetQuizCategoriesQuery): List<QuizCategoryDto> = getVertx().eventBus().queryMsg(query)

    suspend fun deleteQuizCategory(cmd: DeleteQuizCategoryCmd) = getVertx().eventBus().cmdMsg(cmd)

    data class CreateOrUpdateQuizCategoryCmd(val quizStageId: UUID, val quizCategoryDto: QuizCategoryDto) : Cmd {
        companion object : Addressable {
            override val address = "/creator/quiz-category/cmd/create-or-update"
        }

        override val address = Companion.address
    }

    data class GetQuizCategoryQuery(val id: UUID) : Query<QuizCategoryDto?> {
        companion object : Addressable {
            override val address = "/creator/quiz-category/query/get-single"
        }

        override val address = Companion.address
    }

    data class GetQuizCategoriesQuery(val quizStageId: UUID) : Query<List<QuizCategoryDto>> {
        companion object : Addressable {
            override val address = "/creator/quiz-category/query/get-all"
        }

        override val address = Companion.address
    }

    data class DeleteQuizCategoryCmd(val id: UUID) : Cmd {
        companion object : Addressable {
            override val address = "/creator/quiz-category/query/delete"
        }

        override val address = Companion.address
    }
}