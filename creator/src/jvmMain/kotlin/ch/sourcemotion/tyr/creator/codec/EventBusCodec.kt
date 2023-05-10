package ch.sourcemotion.tyr.creator.codec

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageCodec

private val localEventBusCodecName = LocalEventBusCodec.name()

fun EventBus.registerLocalCodec() {
    registerCodec(LocalEventBusCodec)
    codecSelector { localEventBusCodecName }
}

private object LocalEventBusCodec : MessageCodec<Any?, Any?> {
    override fun encodeToWire(buffer: Buffer?, s: Any?) {
        throw UnsupportedOperationException("encodeToWire not supported on local codec")
    }

    override fun decodeFromWire(pos: Int, buffer: Buffer?): Any? {
        throw UnsupportedOperationException("decodeFromWire not supported on local codec")
    }

    override fun transform(s: Any?) = s

    override fun name() = "local-event-bus-coded"

    override fun systemCodecID(): Byte = -1
}