package ch.sourcemotion.tyr.creator.datamapping

import ch.sourcemotion.tyr.creator.domain.MimeType
import ch.sourcemotion.tyr.creator.dto.element.MimeTypeDto

object MimeTypeMapper : Mapper<MimeType, MimeTypeDto> {
    override fun mapToDto(entity: MimeType) = MimeTypeDto.valueOf(entity.name)

    override fun mapToEntity(dto: MimeTypeDto) = MimeType.valueOf(dto.name)
}

fun MimeType.toDto() = MimeTypeMapper.mapToDto(this)
fun MimeTypeDto.toEntity() = MimeTypeMapper.mapToEntity(this)