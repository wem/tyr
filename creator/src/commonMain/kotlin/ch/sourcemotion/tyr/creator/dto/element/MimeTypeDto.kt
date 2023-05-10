package ch.sourcemotion.tyr.creator.dto.element

import kotlinx.serialization.Serializable

@Serializable
enum class MimeTypeDto {
    // Image
    JPEG, PNG, BMP,
    // Sound
    MP3
}