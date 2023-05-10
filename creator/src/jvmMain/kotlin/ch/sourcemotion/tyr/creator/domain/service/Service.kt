package ch.sourcemotion.tyr.creator.domain.service

import io.vertx.core.Vertx

interface Service {
    fun getVertx(): Vertx
}