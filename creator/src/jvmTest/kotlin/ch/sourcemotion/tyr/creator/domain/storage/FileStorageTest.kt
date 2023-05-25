package ch.sourcemotion.tyr.creator.domain.storage

import ch.sourcemotion.tyr.creator.config.FileStorageConfig
import ch.sourcemotion.tyr.creator.domain.MimeType
import ch.sourcemotion.tyr.creator.ext.newUUID
import ch.sourcemotion.tyr.creator.testing.AbstractVertxTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.vertx.core.buffer.Buffer
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class FileStorageTest : AbstractVertxTest() {

    private companion object {
        val fileContent: Buffer = Buffer.buffer("file content")
    }

    @Test
    fun `save and get file - success`(testContext: VertxTestContext, @TempDir tempDir: Path) =
        testContext.async {
            val sut = FileStorage.of(vertx, FileStorageConfig(tempDir.toFile().absolutePath))

            val fileId = newUUID()
            sut.saveFile(fileId, fileContent, MimeType.PNG)
            sut.getFileContent(fileId, MimeType.PNG).shouldBe(fileContent)
        }

    @Test
    fun `delete - success`(testContext: VertxTestContext, @TempDir tempDir: Path) =
        testContext.async {
            val sut = FileStorage.of(vertx, FileStorageConfig(tempDir.toFile().absolutePath))

            val fileId = newUUID()
            sut.saveFile(fileId, fileContent, MimeType.JPEG)

            sut.deleteFile(fileId, MimeType.JPEG)
            shouldThrow<FileNotFoundInStoreException> { sut.getFileContent(fileId, MimeType.JPEG) }
            verifyFileSystemStorageIsEmpty(tempDir)
        }

    @Test
    fun `get - not exists`(testContext: VertxTestContext, @TempDir tempDir: Path) =
        testContext.async {
            val sut = FileStorage.of(vertx, FileStorageConfig(tempDir.toFile().absolutePath))

            val fileId = newUUID()
            shouldThrow<FileNotFoundInStoreException> { sut.getFileContent(fileId, MimeType.JPEG) }
        }

    @Test
    fun `delete - not exists`(testContext: VertxTestContext, @TempDir tempDir: Path) =
        testContext.async {
            val sut = FileStorage.of(vertx, FileStorageConfig(tempDir.toFile().absolutePath))

            val fileId = newUUID()
            shouldThrow<FileNotFoundInStoreException> { sut.deleteFile(fileId, MimeType.JPEG) }
        }

    private suspend fun verifyFileSystemStorageIsEmpty(tempDir: Path) {
        vertx.fileSystem().readDir(tempDir.toFile().absolutePath).await().shouldBeEmpty()
    }
}