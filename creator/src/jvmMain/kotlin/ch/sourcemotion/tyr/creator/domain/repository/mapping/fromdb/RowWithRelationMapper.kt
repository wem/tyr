package ch.sourcemotion.tyr.creator.domain.repository.mapping.fromdb

import ch.sourcemotion.tyr.creator.domain.entity.Entity
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.templates.RowMapper

interface RowWithRelationMapper<E : Entity> : RowMapper<Unit> {
    fun mapWithRelation(fullRow: JsonObject): JsonObject? = null

    fun buildFromRows(): List<E>
}