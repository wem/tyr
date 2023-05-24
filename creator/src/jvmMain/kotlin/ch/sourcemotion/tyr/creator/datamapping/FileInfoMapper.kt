package ch.sourcemotion.tyr.creator.datamapping

import ch.sourcemotion.tyr.creator.domain.entity.FileInfoEntity
import ch.sourcemotion.tyr.creator.dto.FileInfoDto

object FileInfoMapper : Mapper<FileInfoEntity, FileInfoDto> {
    override fun mapToDto(entity: FileInfoEntity) = FileInfoDto(entity.id, entity.mimeType.toDto(), entity.description)

    override fun mapToEntity(dto: FileInfoDto) = FileInfoEntity(dto.id, dto.mimeType.toEntity(), dto.description)
}

fun FileInfoEntity.toDto() = FileInfoMapper.mapToDto(this)
fun FileInfoDto.toEntity() = FileInfoMapper.mapToEntity(this)