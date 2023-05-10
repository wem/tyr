package ch.sourcemotion.tyr.creator.dto

import ch.sourcemotion.tyr.creator.dto.serializer.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class QuizDto(
    @Serializable(with = UuidSerializer::class) val id: Uuid,
    val date: LocalDate,
    val stages: List<QuizStageDto> = emptyList()
)