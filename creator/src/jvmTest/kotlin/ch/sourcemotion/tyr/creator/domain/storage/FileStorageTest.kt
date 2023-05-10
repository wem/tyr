package ch.sourcemotion.tyr.creator.domain.storage

import ch.sourcemotion.tyr.creator.config.FileStorageConfig
import ch.sourcemotion.tyr.creator.domain.MimeType
import ch.sourcemotion.tyr.creator.testing.AbstractVertxTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.vertx.core.buffer.Buffer
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.util.*

class FileStorageTest : AbstractVertxTest() {

    private companion object {
        val fileContent: Buffer = Buffer.buffer("file content")
    }

    @Test
    fun `save and get file - success`(testContext: VertxTestContext, @TempDir tempDir: Path) =
        testContext.async(1) { checkpoint ->
            val fileDescription = "file description"

            val sut = FileStorage.of(vertx, FileStorageConfig(tempDir.toFile().absolutePath), object : FileInfoManager {
                override suspend fun saveFileInfoOf(fileId: UUID, mimeType: MimeType, description: String?) {
                    testContext.verify { description.shouldBe(fileDescription) }
                    checkpoint.flag()
                }

                override suspend fun deleteFileInfoOf(fileId: UUID) {
                    testContext.failNow("delete must not get called")
                }
            })

            val fileId = sut.saveFile(fileContent, MimeType.PNG, fileDescription)
            sut.getFileContent(fileId, MimeType.PNG).shouldBe(fileContent)
        }

    @Test
    fun `save file - file info manager fails`(testContext: VertxTestContext, @TempDir tempDir: Path) =
        testContext.async(1) { checkpoint ->
            val sut = FileStorage.of(vertx, FileStorageConfig(tempDir.toFile().absolutePath), object : FileInfoManager {
                override suspend fun saveFileInfoOf(fileId: UUID, mimeType: MimeType, description: String?) {
                    throw Exception("File info manager did fail")
                }

                override suspend fun deleteFileInfoOf(fileId: UUID) {
                    checkpoint.flag()
                }
            })

            shouldThrow<FileStorageException> { sut.saveFile(fileContent, MimeType.PNG, "description") }
            verifyFileSystemStorageIsEmpty(tempDir)
        }


    @Test
    fun `delete - success`(testContext: VertxTestContext, @TempDir tempDir: Path) =
        testContext.async(2) { checkpoint ->
            var savedFileId: UUID? = null
            val sut = FileStorage.of(vertx, FileStorageConfig(tempDir.toFile().absolutePath), object : FileInfoManager {
                override suspend fun saveFileInfoOf(fileId: UUID, mimeType: MimeType, description: String?) {
                    savedFileId = fileId
                    checkpoint.flag()
                }

                override suspend fun deleteFileInfoOf(fileId: UUID) {
                    testContext.verify { fileId.shouldBe(savedFileId) }
                    checkpoint.flag()
                }
            })

            val fileId = sut.saveFile(fileContent, MimeType.JPEG, "description")
            sut.deleteFile(fileId, MimeType.JPEG)
            sut.getFileContent(fileId, MimeType.PNG).shouldBeNull()
            verifyFileSystemStorageIsEmpty(tempDir)
        }

    @Test
    fun `delete - file info manager fails`(testContext: VertxTestContext, @TempDir tempDir: Path) =
        testContext.async(1) { checkpoint ->
            val sut = FileStorage.of(vertx, FileStorageConfig(tempDir.toFile().absolutePath), object : FileInfoManager {
                override suspend fun saveFileInfoOf(fileId: UUID, mimeType: MimeType, description: String?) {
                    checkpoint.flag()
                }

                override suspend fun deleteFileInfoOf(fileId: UUID) {
                    throw Exception("File info manager did fail")
                }
            })

            val fileId = sut.saveFile(fileContent, MimeType.JPEG, "description")
            sut.deleteFile(fileId, MimeType.JPEG)
            sut.getFileContent(fileId, MimeType.PNG).shouldBeNull()
            verifyFileSystemStorageIsEmpty(tempDir)
        }

    private suspend fun verifyFileSystemStorageIsEmpty(tempDir: Path) {
        vertx.fileSystem().readDir(tempDir.toFile().absolutePath).await().shouldBeEmpty()
    }
}