package ch.sourcemotion.tyr.creator.web.resource.exception

class BadRequestException(message: String? = null, cause: Throwable? = null) : Exception(cause) {
    constructor(cause: Throwable) : this(null, cause)
}