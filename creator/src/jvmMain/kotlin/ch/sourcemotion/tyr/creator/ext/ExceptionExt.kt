package ch.sourcemotion.tyr.creator.ext

fun Throwable.getLastCause() : Throwable = cause?.getLastCause() ?: this