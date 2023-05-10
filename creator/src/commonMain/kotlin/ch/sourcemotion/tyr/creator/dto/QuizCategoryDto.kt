package ch.sourcemotion.tyr.creator.dto

import ch.sourcemotion.tyr.creator.dto.element.TextDto
import ch.sourcemotion.tyr.creator.dto.question.QuestionDto
import ch.sourcemotion.tyr.creator.dto.serializer.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
data class QuizCategoryDto(
    @Serializable(with = UuidSerializer::class) val id: Uuid,
    val title: String,
    val number: Int,
    val contextOrQuestionText: TextDto,
    val questions: List<QuestionDto> = emptyList()
)
