package ch.sourcemotion.tyr.creator.ext

import io.vertx.core.buffer.Buffer

fun Buffer.toUtf8String(): String = toString(Charsets.UTF_8)