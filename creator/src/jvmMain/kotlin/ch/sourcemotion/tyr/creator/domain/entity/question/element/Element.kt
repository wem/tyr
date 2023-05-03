package ch.sourcemotion.tyr.creator.domain.entity.question.element

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_element_type")
@JsonSubTypes(
    JsonSubTypes.Type(Image::class, name = "image"),
    JsonSubTypes.Type(Text::class, name = "text"),
)
interface Element

