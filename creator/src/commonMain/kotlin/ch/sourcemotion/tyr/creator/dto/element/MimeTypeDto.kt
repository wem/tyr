package ch.sourcemotion.tyr.creator.dto.element

import kotlinx.serialization.Serializable

@Serializable
enum class MimeTypeDto(val httpContentType: String) {
    // Image
    JPEG("image/jpeg"), PNG("image/png"), BMP("image/bmp"),
    // Sound
    MP3("audio/mpeg");

    companion object {
        fun ofContentType(httpContentType: String) = values().first { it.httpContentType == httpContentType }
    }
}
