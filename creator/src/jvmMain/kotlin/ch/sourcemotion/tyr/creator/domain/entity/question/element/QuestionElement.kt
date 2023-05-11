package ch.sourcemotion.tyr.creator.domain.entity.question.element

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_element_type")
@JsonSubTypes(
    JsonSubTypes.Type(Text::class, name = "text"),
    JsonSubTypes.Type(Image::class, name = "image"),
    JsonSubTypes.Type(Sound::class, name = "sound"),
)
sealed interface QuestionElement

