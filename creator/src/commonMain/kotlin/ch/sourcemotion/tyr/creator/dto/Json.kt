package ch.sourcemotion.tyr.creator.dto

import ch.sourcemotion.tyr.creator.dto.element.ImageDto
import ch.sourcemotion.tyr.creator.dto.element.QuestionElementDto
import ch.sourcemotion.tyr.creator.dto.element.SoundDto
import ch.sourcemotion.tyr.creator.dto.element.TextDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val dtoModule = SerializersModule {
    polymorphic(QuestionElementDto::class) {
        subclass(TextDto::class, TextDto.serializer())
        subclass(ImageDto::class, ImageDto.serializer())
        subclass(SoundDto::class, SoundDto.serializer())
    }
}

fun jsonDtoSerialization(): Json = Json { serializersModule = dtoModule }