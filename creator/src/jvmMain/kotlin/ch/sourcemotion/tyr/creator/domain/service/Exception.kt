package ch.sourcemotion.tyr.creator.domain.service

import ch.sourcemotion.tyr.creator.exception.CreatorException

open class ServiceException(message: String?, cause: Throwable? = null) : CreatorException(message, cause)