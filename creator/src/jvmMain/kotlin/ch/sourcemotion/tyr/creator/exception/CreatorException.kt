package ch.sourcemotion.tyr.creator.exception

abstract class CreatorException(message: String?, cause: Throwable? = null) : Exception(message, cause)

abstract class CreatorStartException(message: String?, cause: Throwable? = null) : CreatorException(message, cause)