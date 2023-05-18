package ch.sourcemotion.tyr.creator.dto

import ch.sourcemotion.tyr.creator.dto.element.TextDto
import ch.sourcemotion.tyr.creator.dto.question.QuestionDto
import ch.sourcemotion.tyr.creator.dto.serializer.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("QuizCategoryDto")
data class QuizCategoryDto(
    @Serializable(with = UuidSerializer::class) val id: Uuid,
    val title: String,
    val orderNumber: Int,
    val contextOrQuestionText: TextDto,
    val questions: List<QuestionDto> = emptyList()
)
