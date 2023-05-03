package ch.sourcemotion.tyr.creator.domain.entity.question.element

import java.util.*

data class Sound(
    val id: UUID,
    val path: String,
    val mimeType: String,
    val name: String
) : Element