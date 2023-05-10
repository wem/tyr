package ch.sourcemotion.tyr.creator.ext

import io.vertx.ext.web.RequestBody

fun RequestBody.asUtf8String(): String = asString(Charsets.UTF_8.toString())