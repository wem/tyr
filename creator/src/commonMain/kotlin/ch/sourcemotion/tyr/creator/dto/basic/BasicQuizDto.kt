package ch.sourcemotion.tyr.creator.dto.basic

import ch.sourcemotion.tyr.creator.dto.serializer.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class BasicQuizDto(@Serializable(with = UuidSerializer::class) val id: Uuid, val date: LocalDate)