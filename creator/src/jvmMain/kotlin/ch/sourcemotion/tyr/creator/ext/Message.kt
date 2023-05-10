package ch.sourcemotion.tyr.creator.ext

import io.vertx.core.eventbus.Message

fun Message<*>.ack() = reply(null)