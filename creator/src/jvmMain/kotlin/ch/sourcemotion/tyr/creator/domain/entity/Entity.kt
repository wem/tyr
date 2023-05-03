package ch.sourcemotion.tyr.creator.domain.entity

import java.util.UUID

interface Entity {
    companion object {
        const val ID_COLUMN = "id"
    }

    val id: UUID
}