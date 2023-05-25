package ch.sourcemotion.tyr.creator.web.resource

import ch.sourcemotion.tyr.creator.domain.MimeType
import ch.sourcemotion.tyr.creator.domain.service.FileService.*
import ch.sourcemotion.tyr.creator.domain.service.FileServiceVerticleException
import ch.sourcemotion.tyr.creator.domain.storage.FileNotFoundInStoreException
import ch.sourcemotion.tyr.creator.dto.FileInfoDto
import ch.sourcemotion.tyr.creator.dto.element.MimeTypeDto
import ch.sourcemotion.tyr.creator.dto.jsonDtoSerialization
import ch.sourcemotion.tyr.creator.ext.ack
import ch.sourcemotion.tyr.creator.ext.newUUID
import ch.sourcemotion.tyr.creator.ext.toUtf8String
import ch.sourcemotion.tyr.creator.testing.*
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.multipart.MultipartForm
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import io.vertx.uritemplate.UriTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FileResourceTest : AbstractVertxTest(), VertxWebTest {

    private companion object {
        val json = jsonDtoSerialization()

        const val LOCAL_HOST = "localhost"

        const val FILE_ID_PATH_PARAM = "fileId"
        const val CONTENT_TYPE_QUERY_PARAM = "contentType"

        val putFileAndInfoTemplate: UriTemplate = UriTemplate.of("/creator/files")
        val getFilesInfoTemplate: UriTemplate = UriTemplate.of("/creator/files/info")
        val getFileContentAndDeleteFileTemplate: UriTemplate =
            UriTemplate.of("/creator/files/{$FILE_ID_PATH_PARAM}?$CONTENT_TYPE_QUERY_PARAM={$CONTENT_TYPE_QUERY_PARAM}")
    }


    private lateinit var client: WebClient
    private val fileId = newUUID()

    @BeforeEach
    fun setUp() {
        client = WebClient.create(vertx)
    }

    @Test
    fun `put and get file - successful`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        val fileContent = Buffer.buffer(ByteArray(10) { it.toByte() })
        val description = "file-description"
        val mimeType = MimeTypeDto.JPEG

        val form = MultipartForm.create()
        form.attribute("id", "$fileId")
        form.attribute("description", description)
        form.binaryFileUpload("file", "$fileId", fileContent, mimeType.httpContentType)

        vertx.eventBus().consumer<SaveFileCmd>(SaveFileCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd ->
                    cmd.info.shouldBe(FileInfoDto(fileId, mimeType, description))
                    cmd.data.shouldBe(fileContent)
                }
                checkpoint.flag()
                msg.ack()
            }
        }

        vertx.eventBus().consumer<GetFileDataQuery>(GetFileDataQuery.address) { msg ->
            testContext.verify {
                msg.body().asClue { query ->
                    query.id.shouldBe(fileId)
                }
                msg.reply(fileContent)
            }
        }

        client.put(serverPort, LOCAL_HOST, putFileAndInfoTemplate)
            .producesMultiPartForm()
            .sendMultipartForm(form).await().asClue { response ->
                response.shouldBeOk()
            }

        client.get(serverPort, LOCAL_HOST, getFileContentAndDeleteFileTemplate)
            .setTemplateParam(FILE_ID_PATH_PARAM, "$fileId")
            .setTemplateParam(CONTENT_TYPE_QUERY_PARAM, mimeType.httpContentType)
            .send().await().asClue { response ->
                response.shouldBeOk()
                response.bodyAsBuffer().shouldBe(fileContent)
            }
    }

    @Test
    fun `put file - service failure`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val fileContent = Buffer.buffer(ByteArray(10) { it.toByte() })
        val description = "file-description"
        val mimeType = MimeTypeDto.JPEG

        val form = MultipartForm.create()
        form.attribute("id", "$fileId")
        form.attribute("description", description)
        form.binaryFileUpload("file", "$fileId", fileContent, mimeType.httpContentType)

        vertx.eventBus().consumer<SaveFileCmd>(SaveFileCmd.address) { msg ->
            msg.reply(FileServiceVerticleException("Test failure"))
        }

        client.put(serverPort, LOCAL_HOST, putFileAndInfoTemplate)
            .producesMultiPartForm()
            .sendMultipartForm(form).await().asClue { response ->
                response.statusCode().shouldBe(INTERNAL_SERVER_ERROR.code())
            }
    }

    @Test
    fun `put file - missing file content`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val description = "file-description"

        val form = MultipartForm.create()
        form.attribute("id", "$fileId")
        form.attribute("description", description)

        vertx.eventBus().consumer<SaveFileCmd>(SaveFileCmd.address) { msg ->
            msg.reply(FileServiceVerticleException("Test failure"))
        }

        client.put(serverPort, LOCAL_HOST, putFileAndInfoTemplate)
            .producesMultiPartForm()
            .sendMultipartForm(form).await().asClue { response ->
                response.statusCode().shouldBe(BAD_REQUEST.code())
            }
    }

    @Test
    fun `put file - missing file id`(testContext: VertxTestContext) = testContext.async {
        val serverPort = deployCreatorContext()

        val fileContent = Buffer.buffer(ByteArray(10) { it.toByte() })
        val description = "file-description"
        val mimeType = MimeTypeDto.JPEG

        val form = MultipartForm.create()
        form.attribute("description", description)
        form.binaryFileUpload("file", "$fileId", fileContent, mimeType.httpContentType)

        vertx.eventBus().consumer<SaveFileCmd>(SaveFileCmd.address) { msg ->
            msg.reply(FileServiceVerticleException("Test failure"))
        }

        client.put(serverPort, LOCAL_HOST, putFileAndInfoTemplate)
            .producesMultiPartForm()
            .sendMultipartForm(form).await().asClue { response ->
                response.statusCode().shouldBe(BAD_REQUEST.code())
            }
    }

    @Test
    fun `put file and update info - successful`(testContext: VertxTestContext) = testContext.async(2) { checkpoint ->
        val serverPort = deployCreatorContext()

        val fileContent = Buffer.buffer(ByteArray(10) { it.toByte() })
        val description = "file-description"
        val mimeType = MimeTypeDto.JPEG

        val form = MultipartForm.create()
        form.attribute("id", "$fileId")
        form.attribute("description", description)
        form.binaryFileUpload("file", "$fileId", fileContent, mimeType.httpContentType)

        val initialFileInfo = FileInfoDto(fileId, mimeType, description)

        vertx.eventBus().consumer<SaveFileCmd>(SaveFileCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd ->
                    cmd.info.shouldBe(initialFileInfo)
                    cmd.data.shouldBe(fileContent)
                }
                checkpoint.flag()
                msg.ack()
            }
        }

        val updatedFileInfo = FileInfoDto(fileId, MimeTypeDto.JPEG, "new-description")

        vertx.eventBus().consumer<SaveFileInfoCmd>(SaveFileInfoCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd ->
                    cmd.info.shouldBe(updatedFileInfo)
                }
                checkpoint.flag()
                msg.ack()
            }
        }

        vertx.eventBus().consumer<GetFileInfosQuery>(GetFileInfosQuery.address) { msg ->
            msg.reply(listOf(updatedFileInfo))
        }

        client.put(serverPort, LOCAL_HOST, putFileAndInfoTemplate)
            .producesMultiPartForm()
            .sendMultipartForm(form).await().asClue { response ->
                response.statusCode().shouldBe(OK.code())
            }

        client.put(serverPort, LOCAL_HOST, putFileAndInfoTemplate)
            .producesJson()
            .sendBuffer(Buffer.buffer(json.encodeToString(updatedFileInfo))).await().asClue { response ->
                response.statusCode().shouldBe(OK.code())
            }

        client.get(serverPort, LOCAL_HOST, getFilesInfoTemplate)
            .consumesJson()
            .send().await().asClue { response ->
                response.statusCode().shouldBe(OK.code())
                json.decodeFromString<List<FileInfoDto>>(response.bodyAsBuffer().toUtf8String()).asClue { filesInfo ->
                    filesInfo.shouldHaveSize(1).first().shouldBe(updatedFileInfo)
                }
            }
    }

    @Test
    fun `put file and update info - service failure`(testContext: VertxTestContext) =
        testContext.async(1) { checkpoint ->
            val serverPort = deployCreatorContext()

            val fileContent = Buffer.buffer(ByteArray(10) { it.toByte() })
            val description = "file-description"
            val mimeType = MimeTypeDto.JPEG

            val form = MultipartForm.create()
            form.attribute("id", "$fileId")
            form.attribute("description", description)
            form.binaryFileUpload("file", "$fileId", fileContent, mimeType.httpContentType)

            vertx.eventBus().consumer<SaveFileCmd>(SaveFileCmd.address) { msg ->
                testContext.verify {
                    msg.body().asClue { cmd ->
                        cmd.info.shouldBe(FileInfoDto(fileId, mimeType, description))
                        cmd.data.shouldBe(fileContent)
                    }
                    checkpoint.flag()
                    msg.ack()
                }
            }

            vertx.eventBus().consumer<SaveFileInfoCmd>(SaveFileInfoCmd.address) { msg ->
                msg.reply(FileServiceVerticleException("Test failure"))
            }

            client.put(serverPort, LOCAL_HOST, putFileAndInfoTemplate)
                .producesMultiPartForm()
                .sendMultipartForm(form).await().asClue { response ->
                    response.statusCode().shouldBe(OK.code())
                }

            client.put(serverPort, LOCAL_HOST, putFileAndInfoTemplate)
                .producesJson()
                .sendBuffer(
                    Buffer.buffer(json.encodeToString(FileInfoDto(fileId, MimeTypeDto.JPEG, "new-description")))
                )
                .await().asClue { response ->
                    response.statusCode().shouldBe(INTERNAL_SERVER_ERROR.code())
                }
        }

    @Test
    fun `get file - not found`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<GetFileDataQuery>(GetFileDataQuery.address) { msg ->
            checkpoint.flag()
            msg.reply(
                FileServiceVerticleException(
                    "Test exception",
                    FileNotFoundInStoreException(fileId, MimeType.JPEG)
                )
            )
        }

        client.get(serverPort, LOCAL_HOST, getFileContentAndDeleteFileTemplate)
            .setTemplateParam(FILE_ID_PATH_PARAM, "$fileId")
            .setTemplateParam(CONTENT_TYPE_QUERY_PARAM, MimeTypeDto.JPEG.httpContentType)
            .send().await().asClue { response ->
                response.shouldBeNotFound()
            }
    }

    @Test
    fun `delete file - success`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        val mimeType = MimeTypeDto.BMP

        vertx.eventBus().consumer<DeleteFileCmd>(DeleteFileCmd.address) { msg ->
            testContext.verify {
                msg.body().asClue { cmd ->
                    cmd.id.shouldBe(fileId)
                    cmd.mimeType.shouldBe(mimeType)
                }
            }
            checkpoint.flag()
            msg.ack()
        }

        client.delete(serverPort, LOCAL_HOST, getFileContentAndDeleteFileTemplate)
            .setTemplateParam(FILE_ID_PATH_PARAM, "$fileId")
            .setTemplateParam(CONTENT_TYPE_QUERY_PARAM, mimeType.httpContentType)
            .send().await().asClue { response ->
                response.shouldBeOk()
            }
    }

    @Test
    fun `delete file - not found`(testContext: VertxTestContext) = testContext.async(1) { checkpoint ->
        val serverPort = deployCreatorContext()

        vertx.eventBus().consumer<DeleteFileCmd>(DeleteFileCmd.address) { msg ->
            checkpoint.flag()
            msg.reply(
                FileServiceVerticleException(
                    "Test exception",
                    FileNotFoundInStoreException(fileId, MimeType.BMP)
                )
            )
        }

        client.delete(serverPort, LOCAL_HOST, getFileContentAndDeleteFileTemplate)
            .setTemplateParam(FILE_ID_PATH_PARAM, "$fileId")
            .setTemplateParam(CONTENT_TYPE_QUERY_PARAM, MimeTypeDto.BMP.httpContentType)
            .send().await().asClue { response ->
                response.shouldBeNotFound()
            }
    }

    private suspend fun CoroutineScope.deployCreatorContext(): Int {
        return withTestWebServer(vertx) {
            CreatorContext(vertx, this@deployCreatorContext).deploy(this)
        }
    }
}