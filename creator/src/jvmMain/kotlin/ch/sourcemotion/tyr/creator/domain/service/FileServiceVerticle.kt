package ch.sourcemotion.tyr.creator.domain.service

import ch.sourcemotion.tyr.creator.commandquery.onCommand
import ch.sourcemotion.tyr.creator.commandquery.onQuery
import ch.sourcemotion.tyr.creator.datamapping.toDto
import ch.sourcemotion.tyr.creator.datamapping.toEntity
import ch.sourcemotion.tyr.creator.domain.repository.FileInfoRepository
import ch.sourcemotion.tyr.creator.domain.service.FileService.*
import ch.sourcemotion.tyr.creator.domain.storage.FileStorage
import ch.sourcemotion.tyr.creator.dto.FileInfoDto
import ch.sourcemotion.tyr.creator.ext.getOrCreateByFactory
import io.vertx.core.buffer.Buffer
import io.vertx.kotlin.coroutines.CoroutineVerticle

class FileServiceVerticle : CoroutineVerticle(), FileService {

    private lateinit var repo: FileInfoRepository
    private lateinit var storage: FileStorage

    override suspend fun start() {
        repo = vertx.getOrCreateByFactory()
        storage = vertx.getOrCreateByFactory()

        val eventBus = vertx.eventBus()
        eventBus.consumer(GetFileInfosQuery.address, onQuery(::getFileInfos))
        eventBus.consumer(GetFileDataQuery.address, onQuery(::getFileData))

        eventBus.consumer(SaveFileCmd.address, onCommand(::saveFile))
        eventBus.consumer(SaveFileInfoCmd.address, onCommand(::saveFileInfo))
        eventBus.consumer(DeleteFileCmd.address, onCommand(::deleteFile))
    }

    override suspend fun saveFile(cmd: SaveFileCmd) {
        val fileInfo = cmd.info.toEntity()
        runCatching {
            storage.saveFile(cmd.info.id, cmd.data, fileInfo.mimeType)
            runCatching { repo.save(fileInfo) }.onFailure { failure ->
                // In the case the file was saved successfully on the filesystem but in the database,
                // we try to delete file on the filesystem to keep consistency and avoid data garbage.
                storage.deleteFile(fileInfo.id, fileInfo.mimeType)
                throw failure
            }
        }.getOrElse { failure -> throw FileServiceVerticleException("Failed to save file", failure) }
    }

    override suspend fun saveFileInfo(cmd: SaveFileInfoCmd) {
        val fileInfo = cmd.info.toEntity()
        runCatching {
            runCatching { repo.save(fileInfo) }
        }.getOrElse { failure -> throw FileServiceVerticleException("Failed to save file info", failure) }
    }

    override suspend fun deleteFile(cmd: DeleteFileCmd) {
        runCatching {
            // We first delete the database tuple. It's "better" to have a file on the filesystem without a tuple
            // than the opposite
            repo.delete(cmd.info.id)
            storage.deleteFile(cmd.info.id, cmd.info.mimeType.toEntity())
        }.getOrElse { failure -> throw FileServiceVerticleException("Failed to delete file", failure) }
    }

    override suspend fun getFileInfos(query: GetFileInfosQuery): List<FileInfoDto> {
        return runCatching {
            repo.findAll().map { it.toDto() }
        }.getOrElse { failure -> throw FileServiceVerticleException("Failed to get files information", failure) }
    }

    override suspend fun getFileData(query: GetFileDataQuery): Buffer? {
        return runCatching {
            storage.getFileContent(query.id, query.mimeTypeDto.toEntity())
        }.getOrElse { failure -> throw FileServiceVerticleException("Failed to get file content", failure) }
    }
}

class FileServiceVerticleException(message: String?, cause: Throwable? = null) : ServiceException(message, cause)