package ch.sourcemotion.tyr.creator.domain.repository.mapping.fromdb

import ch.sourcemotion.tyr.creator.domain.entity.Entity
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.templates.RowMapper

interface RowWithRelationMapper<E : Entity, J: JsonObject> : RowMapper<Unit> {
    fun mapWithRelation(fullRow: JsonObject): J? = null

    fun buildFromRows(): List<E>
}