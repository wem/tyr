package ch.sourcemotion.tyr.creator.ui.rest

import ch.sourcemotion.tyr.creator.dto.*
import ch.sourcemotion.tyr.creator.dto.element.MimeTypeDto
import com.benasher44.uuid.Uuid
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.charsets.*
import js.buffer.ArrayBuffer
import js.typedarrays.Int8Array
import kotlinx.browser.window
import mu.KotlinLogging

val rest = CreatorClient

object CreatorClient {
    private val logger = KotlinLogging.logger("CreatorClient")

    //    private val baseUrl = "http://localhost:8887/creator"
    private val baseUrl = "${window.location.origin}/creator"

    private val restClient = HttpClient(Js) {
        expectSuccess = true
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
    val files = Files(baseUrl, restClient)
}

class Quizzes(private val baseUrl: String, private val restClient: HttpClient) {
    suspend fun put(quiz: QuizDto) {
        runCatching<Unit> {
            restClient.put("$baseUrl/quizzes") {
                contentType(ContentType.Application.Json)
                setBody(quiz)
            }
        }.getOrElse { failure -> throw RestException("Failed to put quiz '${quiz.id}'", failure) }
    }

    suspend fun delete(quizId: Uuid) {
        runCatching<Unit> {
            restClient.delete("$baseUrl/quizzes/$quizId")
        }.getOrElse { failure -> throw RestException("Failed to delete quiz '$quizId'", failure) }
    }

    suspend fun getAll(withStages: Boolean = false, withCategories: Boolean = false): List<QuizDto> {
        return runCatching {
            restClient.get("$baseUrl/quizzes") {
                accept(ContentType.Application.Json)
                url {
                    parameters.append("withStages", "$withStages")
                    parameters.append("withCategories", "$withCategories")
                }
            }.body<List<QuizDto>>()
        }.getOrElse { failure -> throw RestException("Failed to query all quizzes", failure) }
    }

    suspend fun get(quizId: Uuid, withStages: Boolean = false, withCategories: Boolean = false): QuizDto {
        return runCatching {
            restClient.get("$baseUrl/quizzes/$quizId") {
                accept(ContentType.Application.Json)
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
        runCatching<Unit> {
            restClient.put("$baseUrl/quizzes/$quizId/stages") {
                contentType(ContentType.Application.Json)
                setBody(quizStage)
            }
        }.getOrElse { failure -> throw RestException("Failed to put quiz stage to quiz '$quizId'", failure) }
    }

    suspend fun getAll(quizId: Uuid, withCategories: Boolean = false): List<QuizStageDto> {
        return runCatching {
            restClient.get("$baseUrl/quizzes/$quizId/stages") {
                accept(ContentType.Application.Json)
                url { parameters.append("withCategories", "$withCategories") }
            }.body<List<QuizStageDto>>()
        }.getOrElse { failure -> throw RestException("Failed to query all quiz stages of quiz '$quizId'", failure) }
    }

    suspend fun delete(quizStageId: Uuid) {
        runCatching<Unit> {
            restClient.delete("$baseUrl/stages/$quizStageId")
        }.getOrElse { failure -> throw RestException("Failed to delete quiz stage '$quizStageId'", failure) }
    }

    suspend fun get(quizStageId: Uuid, withCategories: Boolean = false): QuizStageDto {
        return runCatching {
            restClient.get("$baseUrl/stages/$quizStageId") {
                accept(ContentType.Application.Json)
                url { parameters.append("withCategories", "$withCategories") }
            }.body<QuizStageDto>()
        }.getOrElse { failure -> throw RestException("Failed to query quiz stage '$quizStageId'", failure) }
    }
}

class QuizCategories(private val baseUrl: String, private val restClient: HttpClient) {
    suspend fun put(quizStageId: Uuid, quizCategory: QuizCategoryDto) {
        runCatching<Unit> {
            restClient.put("$baseUrl/stages/$quizStageId/categories") {
                contentType(ContentType.Application.Json)
                setBody(quizCategory)
            }
        }.getOrElse { failure -> throw RestException("Failed to query all quizzes from backend", failure) }
    }

    suspend fun getAll(quizStageId: Uuid): List<QuizCategoryDto> {
        return runCatching {
            restClient.get("$baseUrl/stages/$quizStageId/categories") {
                accept(ContentType.Application.Json)
            }.body<List<QuizCategoryDto>>()
        }
            .getOrElse { failure ->
                throw RestException("Failed to query all quiz categories of stage '$quizStageId'", failure)
            }
    }

    suspend fun delete(quizCategoryId: Uuid) {
        runCatching<Unit> {
            restClient.delete("$baseUrl/categories/$quizCategoryId")
        }.getOrElse { failure -> throw RestException("Failed to delete quiz category '$quizCategoryId'", failure) }
    }

    suspend fun get(quizCategoryId: Uuid): QuizCategoryDto {
        return runCatching {
            restClient.get("$baseUrl/categories/$quizCategoryId") {
                accept(ContentType.Application.Json)
            }.body<QuizCategoryDto>()
        }.getOrElse { failure -> throw RestException("Failed to query quiz category '$quizCategoryId'", failure) }
    }
}

class Files(private val baseUrl: String, private val restClient: HttpClient) {
    fun fileContentPathOf(fileInfo: FileInfoDto) =
        "${baseUrl}/files/${fileInfo.id}?contentType=${fileInfo.mimeType.httpContentType}"

    suspend fun put(fileId: Uuid, description: String?, fileData: ArrayBuffer, mimeType: MimeTypeDto) {
        runCatching<Unit> {
            restClient.post("$baseUrl/files") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("id", "$fileId")
                        if (!description.isNullOrEmpty()) {
                            append("description", description)
                        }
                        append("file", Int8Array(fileData).unsafeCast<ByteArray>(), Headers.build {
                            this.append(HttpHeaders.ContentType, mimeType.httpContentType)
                            this.append(HttpHeaders.ContentDisposition, "filename=\"$fileId\"")
                        })
                    },
                    boundary = "CreatorBoundary"
                ))
            }
        }.getOrElse { failure -> throw RestException("Failed to upload file", failure) }
    }

    suspend fun putInfo(quizStageId: Uuid, quizCategory: QuizCategoryDto) {
        runCatching<Unit> {
            restClient.put("$baseUrl/files") {
                contentType(ContentType.Application.Json)
                formData {
                }
                setBody(quizCategory)
            }
        }.getOrElse { failure -> throw RestException("Failed to query all quizzes from backend", failure) }
    }

    suspend fun getFilesInfo(): List<FileInfoDto> {
        return runCatching {
            restClient.get("$baseUrl/files/info") {
                accept(ContentType.Application.Json)
            }.body<List<FileInfoDto>>()
        }.getOrElse { failure -> throw RestException("Failed to query all file infos from backend", failure) }
    }
}

class RestException(message: String?, cause: Throwable? = null) : Exception(message, cause)


