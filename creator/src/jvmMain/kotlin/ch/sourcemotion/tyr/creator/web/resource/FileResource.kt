package ch.sourcemotion.tyr.creator.web.resource

import ch.sourcemotion.tyr.creator.domain.service.FileService
import ch.sourcemotion.tyr.creator.domain.service.FileService.*
import ch.sourcemotion.tyr.creator.domain.storage.FileNotFoundInStoreException
import ch.sourcemotion.tyr.creator.dto.FileInfoDto
import ch.sourcemotion.tyr.creator.dto.element.MimeTypeDto
import ch.sourcemotion.tyr.creator.dto.jsonDtoSerialization
import ch.sourcemotion.tyr.creator.ext.asUtf8String
import ch.sourcemotion.tyr.creator.ext.onFailureAndRethrow
import ch.sourcemotion.tyr.creator.web.resource.exception.BadRequestException
import com.benasher44.uuid.uuidFrom
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON
import io.netty.handler.codec.http.HttpHeaderValues.MULTIPART_FORM_DATA
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.Vertx
import io.vertx.core.file.FileSystem
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KLogging

class FileResource(
    vertx: Vertx,
    scope: CoroutineScope,
    json: Json = jsonDtoSerialization()
) : AbstractResource(vertx, scope, json) {

    private companion object : KLogging() {
        const val FILE_ID_KEY = "fileId"
        const val CONTENT_TYPE_QUERY_PARAM_KEY = "contentType"
    }

    private val filesystem: FileSystem = vertx.fileSystem()
    private val service = FileService.create(vertx)

    override fun deploy(router: Router) {
        router.put("/files").handler(::onPutFile)
            .consumes("$MULTIPART_FORM_DATA")
        router.put("/files").handler(::onPutFileInfo)
            .consumes("$APPLICATION_JSON")

        router.delete("/files/:$FILE_ID_KEY").handler(::onDeleteFile)

        router.get("/files/info").handler(::onGetFilesInfo)
            .produces("$APPLICATION_JSON")
        router.get("/files/:$FILE_ID_KEY").handler(::onGetFileContent)
    }

    private fun onPutFile(rc: RoutingContext) {
        runCatching {
            Triple(
                rc.fileUploads().first(),
                uuidFrom(rc.request().getParam("id")),
                rc.request().getParam("description")
            )
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { (upload, fileId, fileDescription) ->
            rc.withExceptionHandling {
                runCatching {
                    val fileContent = filesystem.readFile(upload.uploadedFileName()).await()
                    val mimeType = MimeTypeDto.ofContentType(upload.contentType())
                    service.saveFile(SaveFileCmd(FileInfoDto(fileId, mimeType, fileDescription), fileContent))
                }.onSuccess {
                    rc.response()
                        .setStatusCode(OK.code())
                        .end()
                    logger.info { "File '$fileId' saved / uploaded" }
                }.onFailureAndRethrow { failure ->
                    logger.error(failure) { "Failed to save / upload file" }
                }
            }
        }
    }

    private fun onPutFileInfo(rc: RoutingContext) {
        runCatching {
            json.decodeFromString(FileInfoDto.serializer(), rc.body().asUtf8String())
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { fileInfo ->
            rc.withExceptionHandling {
                runCatching {
                    service.saveFileInfo(SaveFileInfoCmd(fileInfo))
                }.onSuccess {
                    rc.response()
                        .setStatusCode(OK.code())
                        .end()
                    logger.info { "Info of file '${fileInfo.id}' saved" }
                }.onFailureAndRethrow { failure ->
                    logger.error(failure) { "Failed to save info of file '${fileInfo.id}'" }
                }
            }
        }
    }

    private fun onGetFileContent(rc: RoutingContext) {
        runCatching {
            uuidFrom(rc.pathParam(FILE_ID_KEY)) to
                    MimeTypeDto.ofContentType(rc.queryParam(CONTENT_TYPE_QUERY_PARAM_KEY).first())
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { (fileId, mimeType) ->
            rc.withExceptionHandling {
                runCatching {
                    service.getFileData(GetFileDataQuery(fileId, mimeType))
                }.onSuccess { fileData ->
                        rc.response()
                            .setStatusCode(OK.code())
                            .putHeader(HttpHeaderNames.CONTENT_TYPE, mimeType.httpContentType)
                            .end(fileData)
                }.onFailureAndRethrow { failure ->
                    if (failure.cause is FileNotFoundInStoreException) {
                        rc.response()
                            .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                            .end()
                    }
                    logger.error(failure) { "Failed to get data of file '$fileId'" }
                }
            }
        }
    }

    private fun onGetFilesInfo(rc: RoutingContext) {
        rc.withExceptionHandling {
            runCatching {
                service.getFileInfos(GetFileInfosQuery)
            }.onSuccess { filesInfo ->
                rc.response().setStatusCode(OK.code())
                    .appJsonContentType()
                    .end(json.encodeToString(filesInfo))
            }.onFailureAndRethrow { failure ->
                logger.error(failure) { "Failed to get files info" }
            }
        }
    }

    private fun onDeleteFile(rc: RoutingContext) {
        runCatching {
            uuidFrom(rc.pathParam(FILE_ID_KEY)) to
                    MimeTypeDto.ofContentType(rc.queryParam(CONTENT_TYPE_QUERY_PARAM_KEY).first())
        }.onFailure { failure ->
            throw BadRequestException(failure)
        }.onSuccess { (fileId, mimeType) ->
            rc.withExceptionHandling {
                runCatching {
                    service.deleteFile(DeleteFileCmd(fileId, mimeType))
                }.onSuccess {
                    rc.response()
                        .setStatusCode(OK.code())
                        .end()
                    logger.info { "File '$fileId' deleted" }
                }.onFailureAndRethrow { failure ->
                    if (failure.cause is FileNotFoundInStoreException) {
                        rc.response()
                            .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                            .end()
                    }
                    logger.error(failure) { "Failed to delete file '$fileId'" }
                }
            }
        }
    }
}