package ch.sourcemotion.tyr.creator.domain.storage

import ch.sourcemotion.tyr.creator.config.FileStorageConfig
import ch.sourcemotion.tyr.creator.domain.MimeType
import ch.sourcemotion.tyr.creator.exception.CreatorException
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.*
import mu.KLogging
import java.util.*

interface FileInfoManager {
    suspend fun saveFileInfoOf(fileId: UUID, description: String?)
    suspend fun deleteFileInfoOf(fileId: UUID)
}

class FileStorage private constructor(
    private val basePath: String,
    private val fileSystem: FileSystem,
    private val fileInfoManager: FileInfoManager
) {
    companion object : KLogging() {
        fun of(vertx: Vertx, config: FileStorageConfig, fileInfoManager: FileInfoManager) =
            FileStorage(config.baseFilesystemPath, vertx.fileSystem(), fileInfoManager)

        private val fileExtensionByMimeType =
            mapOf(MimeType.JPEG to "jpg", MimeType.PNG to "png", MimeType.BMP to "bmp", MimeType.MP3 to "mp3")
    }

    suspend fun saveFile(data: Buffer, mimeType: MimeType, description: String?): UUID {
        val fileId = UUID.randomUUID()
        val fileName = fileNameOf(fileId, mimeType)
        runCatching {
            coroutineScope {
                launch { fileSystem.writeFile("$basePath/$fileName", data).await() }
                launch { fileInfoManager.saveFileInfoOf(fileId, description) }
            }
        }.onFailure { failure ->
            logger.error(failure) { "Failed to save file properly. File will get cleaned up" }
            deleteFile(fileId, mimeType)

            throw FileStorageException("File could not be saved", failure)
        }
        return fileId
    }

    suspend fun getFileContent(id: UUID, mimeType: MimeType) : Buffer? {
        val fileName = fileNameOf(id, mimeType)
        val filePath = "${basePath}/$fileName"
        return if (fileSystem.exists(filePath).await()) {
            fileSystem.readFile("${basePath}/$fileName").await()
        } else null
    }

    suspend fun deleteFile(id: UUID, mimeType: MimeType) {
        val fileName = fileNameOf(id, mimeType)
        runCatching {
            val (filesystemDeferred, fileInfoManagerDeferred) = supervisorScope {
                async { fileSystem.delete("${basePath}/$fileName").await() } to
                async { fileInfoManager.deleteFileInfoOf(id) }
            }
            awaitAll(filesystemDeferred, fileInfoManagerDeferred)
        }.onFailure {
            logger.warn { "Failed to delete file '$id' of type '$mimeType'" }
        }
    }

    private fun fileNameOf(fileId: UUID, mimeType: MimeType) =
        "${fileId}.${fileExtensionByMimeType[mimeType]}"
}

class FileStorageException(message: String?, cause: Throwable? = null) : CreatorException(message, cause)