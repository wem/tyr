package ch.sourcemotion.tyr.creator.domain.service

import ch.sourcemotion.tyr.creator.config.FileStorageConfig
import ch.sourcemotion.tyr.creator.datamapping.toEntity
import ch.sourcemotion.tyr.creator.domain.entity.FileInfoEntity
import ch.sourcemotion.tyr.creator.domain.repository.FileInfoRepository
import ch.sourcemotion.tyr.creator.domain.repository.FileInfoRepositoryException
import ch.sourcemotion.tyr.creator.domain.service.FileService.*
import ch.sourcemotion.tyr.creator.domain.storage.FileStorage
import ch.sourcemotion.tyr.creator.dto.FileInfoDto
import ch.sourcemotion.tyr.creator.dto.element.MimeTypeDto
import ch.sourcemotion.tyr.creator.ext.newUUID
import ch.sourcemotion.tyr.creator.ext.shareFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk
import io.vertx.core.buffer.Buffer
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.coroutines.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.util.*

class FileServiceVerticleTest : AbstractServiceVerticleTest() {

    private companion object {
        fun newFileData(): Buffer = Buffer.buffer(ByteArray(10) { it.toByte() })
    }

    private lateinit var sutClient: FileService

    @BeforeEach
    fun onSetUp() {
        sutClient = FileService.create(vertx)
    }

    @Test
    fun `save and get file - success`(testContext: VertxTestContext, @TempDir tempDir: Path) =
        testContext.async(1) { checkpoint ->
            val fileInfoDto = FileInfoDto(newUUID(), MimeTypeDto.JPEG, "description")
            val fileContent = newFileData()

            vertx.shareFactory {
                mockk<FileInfoRepository> {
                    coEvery { save(any()) } answers { call ->
                        testContext.verify {
                            val fileInfoEntity: FileInfoEntity = call.invocation.args.first() as FileInfoEntity
                            fileInfoEntity.shouldBe(fileInfoDto.toEntity())
                        }
                        checkpoint.flag()
                    }
                }
            }

            deploySut(tempDir)

            sutClient.saveFile(SaveFileCmd(fileInfoDto, fileContent))
            sutClient.getFileData(GetFileDataQuery(fileInfoDto.id, fileInfoDto.mimeType)).shouldBe(fileContent)
        }

    @Test
    fun `save and get file - repo failure`(testContext: VertxTestContext, @TempDir tempDir: Path) =
        testContext.async(1) { checkpoint ->
            val fileInfoDto = FileInfoDto(newUUID(), MimeTypeDto.JPEG, "description")
            val fileContent = newFileData()

            vertx.shareFactory {
                mockk<FileInfoRepository> {
                    coEvery { save(any()) } answers { call ->
                        testContext.verify {
                            val fileInfoEntity: FileInfoEntity = call.invocation.args.first() as FileInfoEntity
                            fileInfoEntity.shouldBe(fileInfoDto.toEntity())
                        }
                        checkpoint.flag()
                        throw FileInfoRepositoryException("Test failure")
                    }
                }
            }

            deploySut(tempDir)

            shouldThrow<FileServiceVerticleException> {
                sutClient.saveFile(SaveFileCmd(fileInfoDto, fileContent))
            }.cause.shouldBeInstanceOf<FileInfoRepositoryException>()
            sutClient.getFileData(GetFileDataQuery(fileInfoDto.id, fileInfoDto.mimeType)).shouldBeNull()
        }

    @Test
    fun `save file and update info - success`(testContext: VertxTestContext, @TempDir tempDir: Path) =
        testContext.async(2) { checkpoint ->
            val newDescription = "new description"
            val fileInfoDto = FileInfoDto(newUUID(), MimeTypeDto.JPEG, "description")
            val fileContent = newFileData()

            var saved = 0
            vertx.shareFactory {
                mockk<FileInfoRepository> {
                    coEvery { save(any()) } answers { call ->
                        testContext.verify {
                            if (++saved == 1) {
                                val fileInfoEntity: FileInfoEntity = call.invocation.args.first() as FileInfoEntity
                                fileInfoEntity.shouldBe(fileInfoDto.toEntity())
                            } else {
                                val fileInfoEntity: FileInfoEntity = call.invocation.args.first() as FileInfoEntity
                                fileInfoEntity.shouldBe(fileInfoDto.copy(description = newDescription).toEntity())
                            }
                        }
                        checkpoint.flag()
                    }
                }
            }

            deploySut(tempDir)

            sutClient.saveFile(SaveFileCmd(fileInfoDto, fileContent))
            sutClient.getFileData(GetFileDataQuery(fileInfoDto.id, fileInfoDto.mimeType)).shouldBe(fileContent)
            sutClient.saveFileInfo(SaveFileInfoCmd(fileInfoDto.copy(description = "new description")))
        }

    @Test
    fun `delete file - success`(testContext: VertxTestContext, @TempDir tempDir: Path) =
        testContext.async(2) { checkpoint ->
            val fileInfoDto = FileInfoDto(newUUID(), MimeTypeDto.JPEG, "description")
            val fileContent = newFileData()

            vertx.shareFactory {
                mockk<FileInfoRepository> {
                    coEvery { save(any()) } answers { call ->
                        testContext.verify {
                            val fileInfoEntity: FileInfoEntity = call.invocation.args.first() as FileInfoEntity
                            fileInfoEntity.shouldBe(fileInfoDto.toEntity())
                        }
                        checkpoint.flag()
                    }
                    coEvery { delete(any()) } answers { call ->
                        testContext.verify {
                            val fileId: UUID = call.invocation.args.first() as UUID
                            fileId.shouldBe(fileInfoDto.id)
                        }
                        checkpoint.flag()
                    }
                }
            }

            deploySut(tempDir)

            sutClient.saveFile(SaveFileCmd(fileInfoDto, fileContent))
            sutClient.deleteFile(DeleteFileCmd(fileInfoDto))
            sutClient.getFileData(GetFileDataQuery(fileInfoDto.id, fileInfoDto.mimeType)).shouldBeNull()
        }

    @Test
    fun `delete file - repo failure`(testContext: VertxTestContext, @TempDir tempDir: Path) =
        testContext.async(2) { checkpoint ->
            val fileInfoDto = FileInfoDto(newUUID(), MimeTypeDto.JPEG, "description")
            val fileContent = newFileData()

            vertx.shareFactory {
                mockk<FileInfoRepository> {
                    coEvery { save(any()) } answers { call ->
                        testContext.verify {
                            val fileInfoEntity: FileInfoEntity = call.invocation.args.first() as FileInfoEntity
                            fileInfoEntity.shouldBe(fileInfoDto.toEntity())
                        }
                        checkpoint.flag()
                    }
                    coEvery { delete(any()) } answers { call ->
                        testContext.verify {
                            val fileId: UUID = call.invocation.args.first() as UUID
                            fileId.shouldBe(fileInfoDto.id)
                        }
                        checkpoint.flag()
                        throw FileInfoRepositoryException("Test exception")
                    }
                }
            }

            deploySut(tempDir)

            sutClient.saveFile(SaveFileCmd(fileInfoDto, fileContent))
            shouldThrow<FileServiceVerticleException> { sutClient.deleteFile(DeleteFileCmd(fileInfoDto)) }.cause.shouldBeInstanceOf<FileInfoRepositoryException>()
            sutClient.getFileData(GetFileDataQuery(fileInfoDto.id, fileInfoDto.mimeType)).shouldBe(fileContent)
        }

    private suspend fun deploySut(storageBasePath: Path) {
        vertx.shareFactory { FileStorage.of(vertx, FileStorageConfig(storageBasePath.toFile().absolutePath)) }
        vertx.deployVerticle(FileServiceVerticle::class.java, deploymentOptionsOf()).await()
    }
}