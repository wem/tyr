package ch.sourcemotion.tyr.creator.dto.element

import ch.sourcemotion.tyr.creator.dto.serializer.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
sealed interface QuestionElementDto

@Serializable
data class TextDto(
    val text: String,
    val description: String?,
) : QuestionElementDto {
    companion object {
        fun textDtoOf(value: String) = TextDto(value, null)
    }
}

@Serializable
data class ImageDto(
    @Serializable(with = UuidSerializer::class) val id: Uuid,
    val description: String?,
    val mimeType: MimeTypeDto
) : QuestionElementDto

@Serializable
data class SoundDto(
    @Serializable(with = UuidSerializer::class) val id: Uuid,
    val description: String?,
    val mimeType: MimeTypeDto
) : QuestionElementDto