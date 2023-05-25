package ch.sourcemotion.tyr.creator.domain.service

import ch.sourcemotion.tyr.creator.commandquery.*
import ch.sourcemotion.tyr.creator.dto.FileInfoDto
import ch.sourcemotion.tyr.creator.dto.element.MimeTypeDto
import ch.sourcemotion.tyr.creator.ext.SharedFactory
import ch.sourcemotion.tyr.creator.logging.mdcOf
import com.benasher44.uuid.Uuid
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import kotlinx.coroutines.slf4j.MDCContext

interface FileService : Service {
    companion object : SharedFactory<FileService> {
        fun create(vertx: Vertx): FileService = object : FileService {
            override fun getVertx() = vertx
        }

        override fun createInstance(vertx: Vertx): FileService = create(vertx)
    }


    suspend fun getFileInfos(query: GetFileInfosQuery): List<FileInfoDto> = getVertx().eventBus().queryMsg(query)
    suspend fun getFileData(query: GetFileDataQuery): Buffer = getVertx().eventBus().queryMsg(query)

    suspend fun saveFile(cmd: SaveFileCmd): Unit = getVertx().eventBus().cmdMsg(cmd)
    suspend fun saveFileInfo(cmd: SaveFileInfoCmd): Unit = getVertx().eventBus().cmdMsg(cmd)
    suspend fun deleteFile(cmd: DeleteFileCmd): Unit = getVertx().eventBus().cmdMsg(cmd)

    object GetFileInfosQuery : Query<List<FileInfoDto>>, Addressable {
        override val address = "/creator/file/query/get-file-info-all"
        override fun mdcOf() = MDCContext()
    }


    data class GetFileDataQuery(val id: Uuid, val mimeTypeDto: MimeTypeDto) : Query<Buffer>, Addressable {
        companion object : Addressable {
            override val address = "/creator/file/query/get-file-data"
        }

        override val address = Companion.address
        override fun mdcOf() = mdcOf(fileInfoId = id)
    }

    data class SaveFileCmd(val info: FileInfoDto, val data: Buffer) : Cmd, Addressable {
        companion object : Addressable {
            override val address = "/creator/file/cmd/save-file"
        }

        override val address = Companion.address
        override fun mdcOf() = mdcOf(fileInfoId = info.id)
    }

    data class SaveFileInfoCmd(val info: FileInfoDto) : Cmd, Addressable {
        companion object : Addressable {
            override val address = "/creator/file/cmd/save-file-info"
        }

        override val address = Companion.address
        override fun mdcOf() = mdcOf(fileInfoId = info.id)
    }

    data class DeleteFileCmd(val id: Uuid, val mimeType: MimeTypeDto) : Cmd, Addressable {
        companion object : Addressable {
            override val address = "/creator/file/cmd/delete-file"
        }

        override val address = Companion.address
        override fun mdcOf() = mdcOf(fileInfoId = id)
    }
}