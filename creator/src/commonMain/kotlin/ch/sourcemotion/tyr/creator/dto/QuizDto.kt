package ch.sourcemotion.tyr.creator.dto

import ch.sourcemotion.tyr.creator.dto.serializer.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("QuizDto")
data class QuizDto(
    @Serializable(with = UuidSerializer::class) val id: Uuid,
    val date: LocalDate,
    val stages: List<QuizStageDto> = emptyList()
)