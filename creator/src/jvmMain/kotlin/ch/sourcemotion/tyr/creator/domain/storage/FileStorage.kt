package ch.sourcemotion.tyr.creator.domain.storage

import ch.sourcemotion.tyr.creator.config.FileStorageConfig
import ch.sourcemotion.tyr.creator.domain.MimeType
import ch.sourcemotion.tyr.creator.exception.CreatorException
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.await
import mu.KLogging
import java.util.*

class FileStorage private constructor(
    private val basePath: String,
    private val fileSystem: FileSystem
) {
    companion object : KLogging() {
        fun of(vertx: Vertx, config: FileStorageConfig) = FileStorage(config.baseFilesystemPath, vertx.fileSystem())

        private val fileExtensionByMimeType =
            mapOf(MimeType.JPEG to "jpg", MimeType.PNG to "png", MimeType.BMP to "bmp", MimeType.MP3 to "mp3")
    }

    suspend fun saveFile(id: UUID, data: Buffer, mimeType: MimeType) {
        val fileName = fileNameOf(id, mimeType)
        runCatching {
            fileSystem.writeFile("$basePath/$fileName", data).await()
        }.onFailure { failure ->
            throw FileStorageException("File could not be saved", failure)
        }
    }

    suspend fun getFileContent(id: UUID, mimeType: MimeType): Buffer? {
        val fileName = fileNameOf(id, mimeType)
        val filePath = "${basePath}/$fileName"
        return runCatching { if (fileSystem.exists(filePath).await()) {
            fileSystem.readFile("${basePath}/$fileName").await()
        } else null }.getOrElse {
            throw FileStorageException("Failed to get file")
        }
    }

    suspend fun deleteFile(id: UUID, mimeType: MimeType) {
        val fileName = fileNameOf(id, mimeType)
        runCatching {
            fileSystem.delete("${basePath}/$fileName").await()
        }.onFailure {
            throw FileStorageException("Failed to delete file")
        }
    }

    private fun fileNameOf(fileId: UUID, mimeType: MimeType) =
        "${fileId}.${fileExtensionByMimeType[mimeType]}"
}

class FileStorageException(message: String?, cause: Throwable? = null) : CreatorException(message, cause)