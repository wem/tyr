package ch.sourcemotion.tyr.creator.dto.basic

import ch.sourcemotion.tyr.creator.dto.serializer.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
data class BasicQuizStageDto(
    @Serializable(with = UuidSerializer::class) val id: Uuid,
    val number: Int,
    val description: String?
)