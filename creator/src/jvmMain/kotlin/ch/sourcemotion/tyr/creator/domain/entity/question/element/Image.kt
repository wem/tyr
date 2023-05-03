package ch.sourcemotion.tyr.creator.domain.entity.question.element

import ch.sourcemotion.tyr.creator.domain.MimeType
import java.util.*

data class Image(
    val id: UUID,
    val mimeType: MimeType,
    val description: String?
) : QuestionElement