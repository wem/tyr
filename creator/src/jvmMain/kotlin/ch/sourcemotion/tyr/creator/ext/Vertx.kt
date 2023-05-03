package ch.sourcemotion.tyr.creator.ext

import io.vertx.core.Vertx
import io.vertx.core.shareddata.LocalMap
import io.vertx.core.shareddata.Shareable


fun interface SharedFactory<I : Any> : Shareable {
    fun createInstance(vertx: Vertx): I
}

const val SHARED_FACTORY_MAP_NAME = "shared-factory-map"

inline fun <reified I : Any> Vertx.shareFactory(factory: SharedFactory<I>) {
    getFactoryMap<I>()[I::class.java.name] = factory
}

inline fun <reified I : Any> Vertx.getOrCreateByFactory() : I {
    val typeName = I::class.java.name
    val existingInstance = orCreateContext.get<I?>(typeName)
    return if (existingInstance != null) {
        existingInstance
    } else {
        val factory = getFactoryMap<I>()[typeName]
            ?: throw IllegalStateException("No instance factory shared for type: '$typeName' available")
        factory.createInstance(this).also { orCreateContext.put(typeName, it) }
    }
}

inline fun <reified I : Any> Vertx.getFactoryMap(): LocalMap<String, SharedFactory<I>> =
    sharedData().getLocalMap(SHARED_FACTORY_MAP_NAME)