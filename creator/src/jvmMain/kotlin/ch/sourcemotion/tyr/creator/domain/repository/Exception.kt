package ch.sourcemotion.tyr.creator.domain.repository

import ch.sourcemotion.tyr.creator.exception.CreatorException

open class RepositoryException(message: String?, cause: Throwable? = null) : CreatorException(message, cause)