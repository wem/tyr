package ch.sourcemotion.tyr.creator.ui.rest

import ch.sourcemotion.tyr.creator.dto.QuizCategoryDto
import ch.sourcemotion.tyr.creator.dto.QuizDto
import ch.sourcemotion.tyr.creator.dto.QuizStageDto
import ch.sourcemotion.tyr.creator.dto.jsonDtoSerialization
import com.benasher44.uuid.Uuid
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.charsets.*
import kotlinx.browser.window
import mu.KotlinLogging

val rest = CreatorClient

object CreatorClient {
    private val logger = KotlinLogging.logger("CreatorClient")

//    private val baseUrl = "http://localhost:8887/creator"
    private val baseUrl = "${window.location.origin}/creator"

    private val restClient = HttpClient(Js) {
        Charsets {
            register(Charsets.UTF_8)
        }
        install(ContentNegotiation) {
            json(jsonDtoSerialization())
        }
    }

    val quizzes = Quizzes(baseUrl, restClient)
    val stages = QuizStages(baseUrl, restClient)
    val categories = QuizCategories(baseUrl, restClient)
}

class Quizzes(private val baseUrl: String, private val restClient: HttpClient) {
    suspend fun put(quiz: QuizDto) {
        return runCatching<Unit> {
            restClient.put("$baseUrl/quizzes") {
                setBody(quiz)
                contentType(ContentType.Application.Json)
            }
        }.getOrElse { failure -> throw RestException("Failed to put quiz '${quiz.id}'", failure) }
    }

    suspend fun delete(quizId: Uuid) {
        return runCatching<Unit> {
            restClient.delete("$baseUrl/quizzes/$quizId")
        }.getOrElse { failure -> throw RestException("Failed to delete quiz '$quizId'", failure) }
    }

    suspend fun getAll(withStages: Boolean = false, withCategories: Boolean = false): List<QuizDto> {
        return runCatching { restClient.get("$baseUrl/quizzes"){
            url {
                parameters.append("withStages", "$withStages")
                parameters.append("withCategories", "$withCategories")
            }
        }.body<List<QuizDto>>() }
            .getOrElse { failure -> throw RestException("Failed to query all quizzes", failure) }
    }

    suspend fun get(quizId: Uuid, withStages: Boolean = false, withCategories: Boolean = false): QuizDto {
        return runCatching {
            restClient.get("$baseUrl/quizzes/$quizId"){
                url {
                    parameters.append("withStages", "$withStages")
                    parameters.append("withCategories", "$withCategories")
                }
            }.body<QuizDto>()
        }.getOrElse { failure -> throw RestException("Failed to query quiz '$quizId'", failure) }
    }
}

class QuizStages(private val baseUrl: String, private val restClient: HttpClient) {
    suspend fun put(quizId: Uuid, quizStage: QuizStageDto) {
        return runCatching<Unit> {
            restClient.put("$baseUrl/quizzes/$quizId/stages") {
                contentType(ContentType.Application.Json)
                setBody(quizStage)
            }
        }.getOrElse { failure -> throw RestException("Failed to put quiz stage to quiz '$quizId'", failure) }
    }

    suspend fun delete(quizStageId: Uuid) {
        return runCatching<Unit> {
            restClient.delete("$baseUrl/stages/$quizStageId")
        }.getOrElse { failure -> throw RestException("Failed to delete quiz stage '$quizStageId'", failure) }
    }

    suspend fun getAll(quizId: Uuid, withCategories: Boolean = false): List<QuizStageDto> {
        return runCatching { restClient.get("$baseUrl/quizzes/$quizId/stages"){
            url { parameters.append("withCategories", "$withCategories") }
        }.body<List<QuizStageDto>>() }
            .getOrElse { failure -> throw RestException("Failed to query all quiz stages of quiz '$quizId'", failure) }
    }

    suspend fun get(quizStageId: Uuid, withCategories: Boolean = false): QuizDto {
        return runCatching {
            restClient.get("$baseUrl/stages/$quizStageId"){
                url { parameters.append("withCategories", "$withCategories") }
            }.body<QuizDto>()
        }.getOrElse { failure -> throw RestException("Failed to query quiz stage '$quizStageId'", failure) }
    }
}

class QuizCategories(private val baseUrl: String, private val restClient: HttpClient) {
    suspend fun put(quizStageId: Uuid, quizCategory: QuizCategoryDto) {
        return runCatching<Unit> {
            restClient.put("$baseUrl/stages/$quizStageId/categories") {
                contentType(ContentType.Application.Json)
                setBody(quizCategory)
            }
        }.getOrElse { failure -> throw RestException("Failed to query all quizzes from backend", failure) }
    }

    suspend fun getAll(quizStageId: Uuid): List<QuizDto> {
        return runCatching { restClient.get("$baseUrl/stages/$quizStageId/categories").body<List<QuizDto>>() }
            .getOrElse { failure ->
                throw RestException("Failed to query all quiz categories of stage '$quizStageId'", failure)
            }
    }

    suspend fun delete(quizCategoryId: Uuid) {
        return runCatching<Unit> {
            restClient.delete("$baseUrl/categories/$quizCategoryId")
        }.getOrElse { failure -> throw RestException("Failed to delete quiz category '$quizCategoryId'", failure) }
    }

    suspend fun get(quizCategoryId: Uuid): QuizDto {
        return runCatching {
            restClient.get("$baseUrl/categories/$quizCategoryId").body<QuizDto>()
        }.getOrElse { failure -> throw RestException("Failed to query quiz category '$quizCategoryId'", failure) }
    }
}

class RestException(message: String?, cause: Throwable?) : Exception(message, cause)


