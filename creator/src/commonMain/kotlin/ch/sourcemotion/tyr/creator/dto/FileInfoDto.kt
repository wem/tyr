package ch.sourcemotion.tyr.creator.dto

import ch.sourcemotion.tyr.creator.dto.element.MimeTypeDto
import ch.sourcemotion.tyr.creator.dto.serializer.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable

data class FileInfoDto(
    @Serializable(with = UuidSerializer::class) val id: Uuid,
    val mimeType: MimeTypeDto,
    val description: String?
)
