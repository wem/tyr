package ch.sourcemotion.tyr.creator.ext

import io.vertx.core.json.JsonObject

fun Any.encodeToJson() : String = JsonObject.mapFrom(this).encode()