package ch.sourcemotion.tyr.creator

import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlin.reflect.KClass

abstract class VerticleWithOptions<O : Any>(private val optionsClass: KClass<O>) : CoroutineVerticle() {

    protected lateinit var options: O

    override suspend fun start() {
        options = config.mapTo(optionsClass.java)
    }
}